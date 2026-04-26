package com.example.seabattle.data

import com.example.seabattle.game.GameEngine
import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus
import com.example.seabattle.model.GameSummary
import com.example.seabattle.model.PendingShot
import com.example.seabattle.model.Profile
import com.example.seabattle.model.Ship
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GameRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun createGame(hostUid: String, hostProfile: Profile): String {
        val gameId = buildGameId()
        val now = System.currentTimeMillis()

        firestore.collection(FirebaseRefs.Games)
            .document(gameId)
            .set(
                mapOf(
                    "gameId" to gameId,
                    "hostUid" to hostUid,
                    "guestUid" to null,
                    "hostProfile" to profileToMap(hostProfile),
                    "guestProfile" to null,
                    "status" to GameStatus.WAITING_FOR_GUEST.name,
                    "currentTurnUid" to hostUid,
                    "hostReady" to false,
                    "guestReady" to false,
                    "hostShips" to emptyList<Map<String, Any>>(),
                    "guestShips" to emptyList<Map<String, Any>>(),
                    "hostShotsReceived" to emptyList<Int>(),
                    "guestShotsReceived" to emptyList<Int>(),
                    "hostShotsMade" to emptyList<Int>(),
                    "guestShotsMade" to emptyList<Int>(),
                    "pendingGuestShot" to null,
                    "lastProcessedGuestRequestId" to null,
                    "winnerUid" to null,
                    "createdAt" to now,
                    "updatedAt" to now,
                )
            )
            .await()

        return gameId
    }

    suspend fun joinGame(gameId: String, guestUid: String, guestProfile: Profile) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            check(snapshot.exists()) { "Game not found" }
            check(snapshot.getString("guestUid").isNullOrBlank()) { "Game is already full" }
            check(snapshot.getString("hostUid") != guestUid) { "Host cannot join as guest" }

            transaction.update(
                document,
                mapOf(
                    "guestUid" to guestUid,
                    "guestProfile" to profileToMap(guestProfile),
                    "status" to GameStatus.READY_CHECK.name,
                    "updatedAt" to System.currentTimeMillis(),
                )
            )
        }.await()
    }

    suspend fun markReady(gameId: String, uid: String, ships: List<Ship>) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            check(snapshot.exists()) { "Game not found" }
            val hostUid = snapshot.getString("hostUid").orEmpty()
            val guestUid = snapshot.getString("guestUid").orEmpty()
            val isHost = uid == hostUid
            check(isHost || uid == guestUid) { "Only participants can ready up" }

            val updates = linkedMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis(),
                "status" to GameStatus.READY_CHECK.name,
            )

            if (isHost) {
                updates["hostReady"] = true
                updates["hostShips"] = ships.map(::shipToMap)
            } else {
                updates["guestReady"] = true
                updates["guestShips"] = ships.map(::shipToMap)
            }

            val hostReady = if (isHost) true else snapshot.getBoolean("hostReady") == true
            val guestReady = if (!isHost) true else snapshot.getBoolean("guestReady") == true
            if (hostReady && guestReady) {
                updates["status"] = GameStatus.HOST_TURN.name
                updates["currentTurnUid"] = hostUid
            }

            transaction.update(document, updates)
        }.await()
    }

    fun observeGame(gameId: String): Flow<GameState?> = callbackFlow {
        val listener = firestore.collection(FirebaseRefs.Games)
            .document(gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toGameState())
            }

        awaitClose { listener.remove() }
    }

    fun observeHistory(uid: String): Flow<List<GameSummary>> = callbackFlow {
        val listener = firestore.collection(FirebaseRefs.CompletedGames)
            .whereArrayContains("playerIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val summaries = snapshot?.documents
                    .orEmpty()
                    .mapNotNull { it.toGameSummary(uid) }
                    .sortedByDescending { it.finishedAt }

                trySend(summaries)
            }

        awaitClose { listener.remove() }
    }

    suspend fun hostFire(gameId: String, hostUid: String, cellIndex: Int) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        firestore.runTransaction { transaction ->
            val currentState = transaction.get(document).toGameState()
            checkNotNull(currentState) { "Game not found" }
            check(currentState.hostUid == hostUid) { "Only host can resolve host shots" }
            val updatedState = GameEngine.applyHostShot(currentState, cellIndex)
            transaction.set(document, gameStateToMap(updatedState))
            if (updatedState.isFinished) {
                transaction.set(
                    firestore.collection(FirebaseRefs.CompletedGames).document(gameId),
                    gameSummaryToMap(updatedState),
                )
            }
        }.await()
    }

    suspend fun submitGuestShot(gameId: String, guestUid: String, cellIndex: Int) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        val requestId = UUID.randomUUID().toString()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            check(snapshot.exists()) { "Game not found" }
            check(snapshot.getString("guestUid") == guestUid) { "Only guest can submit a guest shot" }
            check(snapshot.getString("status") == GameStatus.GUEST_TURN.name) { "It is not guest turn" }
            check((snapshot.get("pendingGuestShot") as? Map<*, *>) == null) { "Previous guest shot is still being processed" }
            check(cellIndex in 0..24) { "Shot is out of board range" }
            val shotsMade = ((snapshot.get("guestShotsMade") as? List<*>) ?: emptyList<Any>())
                .mapNotNull { (it as? Number)?.toInt() }
            check(cellIndex !in shotsMade) { "Cell already targeted" }

            transaction.update(
                document,
                mapOf(
                    "pendingGuestShot" to mapOf(
                        "requestId" to requestId,
                        "shooterUid" to guestUid,
                        "cellIndex" to cellIndex,
                    ),
                    "updatedAt" to System.currentTimeMillis(),
                )
            )
        }.await()
    }

    suspend fun leaveGame(gameId: String, uid: String) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            if (!snapshot.exists()) return@runTransaction

            val hostUid = snapshot.getString("hostUid").orEmpty()
            val guestUid = snapshot.getString("guestUid").orEmpty()

            when (uid) {
                hostUid -> transaction.delete(document)
                guestUid -> transaction.update(
                    document,
                    mapOf(
                        "guestUid" to null,
                        "guestProfile" to null,
                        "guestReady" to false,
                        "guestShips" to emptyList<Map<String, Any>>(),
                        "guestShotsReceived" to emptyList<Int>(),
                        "guestShotsMade" to emptyList<Int>(),
                        "pendingGuestShot" to null,
                        "lastProcessedGuestRequestId" to null,
                        "winnerUid" to null,
                        "status" to GameStatus.WAITING_FOR_GUEST.name,
                        "currentTurnUid" to hostUid,
                        "updatedAt" to System.currentTimeMillis(),
                    )
                )
            }
        }.await()
    }

    suspend fun processPendingGuestShot(gameId: String, hostUid: String) {
        val document = firestore.collection(FirebaseRefs.Games).document(gameId)
        firestore.runTransaction { transaction ->
            val currentState = transaction.get(document).toGameState()
            checkNotNull(currentState) { "Game not found" }
            check(currentState.hostUid == hostUid) { "Only host can process guest shots" }

            val pendingShot = currentState.pendingGuestShot ?: return@runTransaction
            if (pendingShot.requestId == currentState.lastProcessedGuestRequestId) return@runTransaction

            val updatedState = GameEngine.applyGuestShot(currentState, pendingShot.cellIndex).copy(
                pendingGuestShot = null,
                lastProcessedGuestRequestId = pendingShot.requestId,
            )

            transaction.set(document, gameStateToMap(updatedState))
            if (updatedState.isFinished) {
                transaction.set(
                    firestore.collection(FirebaseRefs.CompletedGames).document(gameId),
                    gameSummaryToMap(updatedState),
                )
            }
        }.await()
    }

    private fun buildGameId(): String {
        val raw = UUID.randomUUID().toString().replace("-", "")
        return raw.take(6).uppercase()
    }

    private fun profileToMap(profile: Profile): Map<String, Any> = mapOf(
        "uid" to profile.uid,
        "nickname" to profile.nickname,
        "avatarChoice" to profile.avatarChoice.storageName,
    )

    private fun shipToMap(ship: Ship): Map<String, Any> = mapOf(
        "size" to ship.size,
        "cells" to ship.cells,
    )

    private fun pendingShotToMap(pendingShot: PendingShot): Map<String, Any> = mapOf(
        "requestId" to pendingShot.requestId,
        "shooterUid" to pendingShot.shooterUid,
        "cellIndex" to pendingShot.cellIndex,
    )

    private fun gameStateToMap(state: GameState): Map<String, Any?> = mapOf(
        "gameId" to state.gameId,
        "hostUid" to state.hostUid,
        "guestUid" to state.guestUid,
        "hostProfile" to state.hostProfile?.let(::profileToMap),
        "guestProfile" to state.guestProfile?.let(::profileToMap),
        "status" to state.status.name,
        "currentTurnUid" to state.currentTurnUid,
        "hostReady" to state.hostReady,
        "guestReady" to state.guestReady,
        "hostShips" to state.hostShips.map(::shipToMap),
        "guestShips" to state.guestShips.map(::shipToMap),
        "hostShotsReceived" to state.hostShotsReceived,
        "guestShotsReceived" to state.guestShotsReceived,
        "hostShotsMade" to state.hostShotsMade,
        "guestShotsMade" to state.guestShotsMade,
        "pendingGuestShot" to state.pendingGuestShot?.let(::pendingShotToMap),
        "lastProcessedGuestRequestId" to state.lastProcessedGuestRequestId,
        "winnerUid" to state.winnerUid,
        "createdAt" to state.createdAt,
        "updatedAt" to state.updatedAt,
    )

    private fun gameSummaryToMap(state: GameState): Map<String, Any> {
        val hostName = state.hostProfile?.nickname.orEmpty()
        val guestName = state.guestProfile?.nickname.orEmpty()
        return buildMap {
            put("gameId", state.gameId)
            put("hostUid", state.hostUid)
            state.guestUid?.let { put("guestUid", it) }
            put("hostName", hostName)
            put("guestName", guestName)
            state.winnerUid?.let { put("winnerUid", it) }
            put("finishedAt", System.currentTimeMillis())
            put("playerIds", listOfNotNull(state.hostUid, state.guestUid))
            put("totalTurns", state.hostShotsMade.size + state.guestShotsMade.size)
        }
    }
}

private fun DocumentSnapshot.toGameState(): GameState? {
    if (!exists()) return null

    return GameState(
        gameId = getString("gameId").orEmpty(),
        hostUid = getString("hostUid").orEmpty(),
        guestUid = getString("guestUid"),
        hostProfile = (get("hostProfile") as? Map<*, *>)?.toProfile(),
        guestProfile = (get("guestProfile") as? Map<*, *>)?.toProfile(),
        status = getString("status")?.let { GameStatus.valueOf(it) } ?: GameStatus.WAITING_FOR_GUEST,
        currentTurnUid = getString("currentTurnUid").orEmpty(),
        hostReady = getBoolean("hostReady") == true,
        guestReady = getBoolean("guestReady") == true,
        hostShips = ((get("hostShips") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Map<*, *>)?.toShip() },
        guestShips = ((get("guestShips") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Map<*, *>)?.toShip() },
        hostShotsReceived = ((get("hostShotsReceived") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Number)?.toInt() },
        guestShotsReceived = ((get("guestShotsReceived") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Number)?.toInt() },
        hostShotsMade = ((get("hostShotsMade") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Number)?.toInt() },
        guestShotsMade = ((get("guestShotsMade") as? List<*>) ?: emptyList<Any>()).mapNotNull { (it as? Number)?.toInt() },
        pendingGuestShot = (get("pendingGuestShot") as? Map<*, *>)?.toPendingShot(),
        lastProcessedGuestRequestId = getString("lastProcessedGuestRequestId"),
        winnerUid = getString("winnerUid"),
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,
    )
}

private fun Map<*, *>.toProfile(): Profile? {
    val uid = this["uid"] as? String ?: return null
    val nickname = this["nickname"] as? String ?: ""
    val avatarChoice = com.example.seabattle.model.avatarChoiceFromStorageName(this["avatarChoice"] as? String)
        ?: com.example.seabattle.model.AvatarChoice.CAPTAIN
    return Profile(uid, nickname, avatarChoice)
}

private fun Map<*, *>.toShip(): Ship? {
    val size = (this["size"] as? Number)?.toInt() ?: return null
    val cells = (this["cells"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()
    return Ship(size = size, cells = cells)
}

private fun Map<*, *>.toPendingShot(): PendingShot? {
    val requestId = this["requestId"] as? String ?: return null
    return PendingShot(
        requestId = requestId,
        shooterUid = this["shooterUid"] as? String ?: "",
        cellIndex = (this["cellIndex"] as? Number)?.toInt() ?: -1,
    )
}

private fun DocumentSnapshot.toGameSummary(currentUid: String): GameSummary? {
    val gameId = getString("gameId") ?: id
    val winnerUid = getString("winnerUid")
    val hostUid = getString("hostUid").orEmpty()
    val guestUid = getString("guestUid").orEmpty()
    val opponentName = if (currentUid == hostUid) {
        getString("guestName").orEmpty()
    } else {
        getString("hostName").orEmpty()
    }

    return GameSummary(
        gameId = gameId,
        opponentName = opponentName.ifBlank { if (currentUid == hostUid) guestUid else hostUid },
        didWin = winnerUid == currentUid,
        finishedAt = getLong("finishedAt") ?: 0L,
        totalTurns = getLong("totalTurns")?.toInt() ?: 0,
    )
}
