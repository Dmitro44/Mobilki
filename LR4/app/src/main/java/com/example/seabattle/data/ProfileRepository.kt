package com.example.seabattle.data

import com.example.seabattle.model.AvatarChoice
import com.example.seabattle.model.Profile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun getProfile(uid: String): Profile? {
        val snapshot = firestore.collection(FirebaseRefs.Profiles).document(uid).get().await()
        if (!snapshot.exists()) return null

        val avatarChoice = com.example.seabattle.model.avatarChoiceFromStorageName(snapshot.getString("avatarChoice"))
            ?: AvatarChoice.VRUNGEL

        return Profile(
            uid = uid,
            nickname = snapshot.getString("nickname").orEmpty(),
            avatarChoice = avatarChoice,
        )
    }

    suspend fun saveProfile(uid: String, nickname: String, avatarChoice: AvatarChoice): Profile {
        val profile = Profile(
            uid = uid,
            nickname = nickname.trim(),
            avatarChoice = avatarChoice,
        )

        firestore.collection(FirebaseRefs.Profiles)
            .document(uid)
            .set(
                mapOf(
                    "uid" to profile.uid,
                    "nickname" to profile.nickname,
                    "avatarChoice" to profile.avatarChoice.storageName,
                )
            )
            .await()

        return profile
    }
}
