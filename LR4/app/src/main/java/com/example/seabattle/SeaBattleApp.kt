package com.example.seabattle

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seabattle.data.AppContainer
import com.example.seabattle.game.FleetRules
import com.example.seabattle.model.AvatarChoice
import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus
import com.example.seabattle.model.Profile
import com.example.seabattle.model.Ship
import com.example.seabattle.viewmodel.AppUiState
import com.example.seabattle.viewmodel.AppViewModel
import com.example.seabattle.viewmodel.GameUiState
import com.example.seabattle.viewmodel.GameViewModel
import com.example.seabattle.viewmodel.HistoryUiState
import com.example.seabattle.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object AppRoute {
    const val Auth = "auth"
    const val Home = "home"
    const val Profile = "profile"
    const val CreateJoin = "create_join"
    const val Lobby = "lobby"
    const val Battle = "battle"
    const val History = "history"
}

@Composable
fun SeaBattleApp() {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current
    val appContainer = remember(context) {
        if (inspectionMode) null else (context.applicationContext as SeaBattleApplication).appContainer
    }

    val appViewModel: AppViewModel = viewModel(
        factory = simpleFactory {
            AppViewModel(
                isFirebaseConfigured = appContainer?.isFirebaseConfigured == true,
                authRepository = appContainer?.authRepository,
                profileRepository = appContainer?.profileRepository,
            )
        }
    )
    val gameViewModel: GameViewModel = viewModel(
        factory = simpleFactory { GameViewModel(appContainer?.gameRepository) }
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = simpleFactory { HistoryViewModel(appContainer?.gameRepository) }
    )

    val appUiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    var showBattleLeaveConfirmation by rememberSaveable { mutableStateOf(false) }
    var suppressedGameId by rememberSaveable { mutableStateOf<String?>(null) }
    val activity = context as? Activity
    val clipboardManager = remember(context) {
        context.getSystemService(ClipboardManager::class.java)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        appViewModel.setNotificationsEnabled(granted)
    }

    LaunchedEffect(appUiState.currentUserId) {
        val uid = appUiState.currentUserId ?: return@LaunchedEffect
        historyViewModel.observeHistory(uid)
    }

    LaunchedEffect(Unit) {
        appViewModel.setNotificationsEnabled(
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        )
    }

    LaunchedEffect(
        appUiState.currentUserId,
        appUiState.profile?.isComplete,
        currentRoute,
        previousRoute,
        appUiState.isFirebaseConfigured,
    ) {
        if (!appUiState.isFirebaseConfigured) return@LaunchedEffect

        when {
            appUiState.currentUserId == null -> {
                if (currentRoute != AppRoute.Auth) {
                    navController.navigate(AppRoute.Auth) {
                        popUpTo(AppRoute.Auth) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            appUiState.profile?.isComplete != true -> {
                if (currentRoute != AppRoute.Profile) {
                    navController.navigate(AppRoute.Profile) {
                        popUpTo(AppRoute.Auth) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            currentRoute == AppRoute.Auth ||
                (currentRoute == AppRoute.Profile && previousRoute == AppRoute.Auth) -> {
                navController.navigate(AppRoute.Home) {
                    popUpTo(AppRoute.Auth) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(gameUiState.gameId, gameUiState.currentGame, currentRoute) {
        if (gameUiState.gameId.isNotBlank() && gameUiState.currentGame == null && currentRoute in setOf(AppRoute.Lobby, AppRoute.Battle)) {
            navController.navigate(AppRoute.Home) {
                popUpTo(AppRoute.Home) { inclusive = false }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        val game = gameUiState.currentGame ?: return@LaunchedEffect
        if (game.gameId == suppressedGameId) return@LaunchedEffect

        when (game.status) {
            GameStatus.WAITING_FOR_GUEST,
            GameStatus.READY_CHECK -> {
                if (currentRoute != AppRoute.Lobby) {
                    navController.navigateSingleTop(AppRoute.Lobby)
                }
            }

            GameStatus.HOST_TURN,
            GameStatus.GUEST_TURN,
            GameStatus.FINISHED -> {
                if (currentRoute != AppRoute.Battle) {
                    navController.navigateSingleTop(AppRoute.Battle)
                }
            }
        }
    }

    LaunchedEffect(gameUiState.gameId, gameUiState.currentGame, currentRoute) {
        if (currentRoute == AppRoute.Home && gameUiState.gameId.isNotBlank() && gameUiState.currentGame == null) {
            gameViewModel.clearLocalGame()
        }
    }

    if (!appUiState.isFirebaseConfigured) {
        FirebaseSetupRequiredScreen()
        return
    }

    val showBackButton = when (currentRoute) {
        AppRoute.Profile -> appUiState.profile?.isComplete == true
        AppRoute.CreateJoin,
        AppRoute.History,
        AppRoute.Lobby,
        AppRoute.Battle -> true
        else -> false
    }

    val leaveLobbyAndGoToCreateJoin: () -> Unit = {
        suppressedGameId = gameUiState.currentGame?.gameId ?: gameUiState.gameId.takeIf { it.isNotBlank() }
        val currentUserId = appUiState.currentUserId
        if (currentUserId != null) {
            gameViewModel.leaveGame(currentUserId)
        } else {
            gameViewModel.clearLocalGame()
        }
        if (!navController.popBackStack(AppRoute.CreateJoin, inclusive = false)) {
            navController.navigate(AppRoute.CreateJoin) {
                popUpTo(AppRoute.Lobby) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val finishBattleAndGoHome: () -> Unit = {
        suppressedGameId = gameUiState.currentGame?.gameId ?: gameUiState.gameId.takeIf { it.isNotBlank() }
        gameViewModel.clearLocalGame()
        if (!navController.popBackStack(AppRoute.Home, inclusive = false)) {
            navController.navigate(AppRoute.Home) {
                launchSingleTop = true
            }
        }
    }

    val confirmBattleLeaveAndGoHome: () -> Unit = {
        suppressedGameId = gameUiState.currentGame?.gameId ?: gameUiState.gameId.takeIf { it.isNotBlank() }
        val currentUserId = appUiState.currentUserId
        if (currentUserId != null) {
            gameViewModel.leaveGame(currentUserId)
        } else {
            gameViewModel.clearLocalGame()
        }
        showBattleLeaveConfirmation = false
        if (!navController.popBackStack(AppRoute.Home, inclusive = false)) {
            navController.navigate(AppRoute.Home) {
                launchSingleTop = true
            }
        }
    }

    val battleBackAction: () -> Unit = {
        val currentGame = gameUiState.currentGame
        if (currentGame?.isFinished == true) {
            finishBattleAndGoHome()
        } else {
            showBattleLeaveConfirmation = true
        }
    }

    val logout: () -> Unit = {
        val currentUserId = appUiState.currentUserId
        if (currentUserId != null) {
            gameViewModel.leaveGame(currentUserId)
        } else {
            gameViewModel.clearLocalGame()
        }
        appViewModel.signOut()
    }

    val topBarBackAction: () -> Unit = {
        when (currentRoute) {
            AppRoute.CreateJoin,
            AppRoute.History,
            AppRoute.Profile -> navController.popBackStack()

            AppRoute.Lobby -> leaveLobbyAndGoToCreateJoin()

            AppRoute.Battle -> battleBackAction()

            else -> Unit
        }
    }

    if (showBattleLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showBattleLeaveConfirmation = false },
            title = { Text("Leave battle?") },
            text = { Text("You will leave the current match and return to the main menu.") },
            confirmButton = {
                TextButton(onClick = confirmBattleLeaveAndGoHome) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBattleLeaveConfirmation = false }) {
                    Text("Stay")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                canGoBack = showBackButton,
                onBackClick = topBarBackAction,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Auth,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Auth) {
                AuthRoute(
                    appUiState = appUiState,
                    onSignIn = appViewModel::signIn,
                    onRegister = appViewModel::register,
                )
            }

            composable(AppRoute.Profile) {
                ProfileRoute(
                    appUiState = appUiState,
                    historyUiState = historyUiState,
                    onSaveProfile = { nickname, avatar ->
                        appViewModel.saveProfile(nickname, avatar)
                    },
                    onRequestNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenNotificationSettings = {
                        activity?.openNotificationSettings()
                    }
                )
            }

            composable(AppRoute.Home) {
                HomeRoute(
                    appUiState = appUiState,
                    gameUiState = gameUiState,
                    onCreateJoinClick = { navController.navigateSingleTop(AppRoute.CreateJoin) },
                    onProfileClick = { navController.navigateSingleTop(AppRoute.Profile) },
                    onHistoryClick = { navController.navigateSingleTop(AppRoute.History) },
                    onLogoutClick = logout,
                    onResumeLobbyClick = {
                        suppressedGameId = null
                        val target = when (gameUiState.currentGame?.status) {
                            GameStatus.HOST_TURN,
                            GameStatus.GUEST_TURN,
                            GameStatus.FINISHED -> AppRoute.Battle
                            else -> AppRoute.Lobby
                        }
                        navController.navigateSingleTop(target)
                    },
                    onShareLobbyCodeClick = {
                        gameUiState.gameId.takeIf { it.isNotBlank() }?.let { gameId ->
                            shareGameCode(activity, clipboardManager, gameId)
                        }
                    }
                )
            }

            composable(AppRoute.CreateJoin) {
                CreateJoinRoute(
                    appUiState = appUiState,
                    gameUiState = gameUiState,
                    onCreateLobby = {
                        suppressedGameId = null
                        appUiState.profile?.let(gameViewModel::createGame)
                    },
                    onJoinLobby = { code ->
                        suppressedGameId = null
                        val profile = appUiState.profile ?: return@CreateJoinRoute
                        gameViewModel.joinGame(code, profile)
                    }
                )
            }

            composable(AppRoute.Lobby) {
                BackHandler {
                    leaveLobbyAndGoToCreateJoin()
                }

                LobbyRoute(
                    appUiState = appUiState,
                    gameUiState = gameUiState,
                    onReadyButtonClick = {
                        val currentUserId = appUiState.currentUserId ?: return@LobbyRoute
                        val isCurrentlyReady = gameUiState.currentGame?.isReadyFor(currentUserId) == true
                        gameViewModel.setReady(currentUserId, !isCurrentlyReady)
                    },
                    onSelectShipSize = gameViewModel::selectShipSize,
                    onToggleOrientation = gameViewModel::togglePlacementOrientation,
                    onBoardCellClick = { row, column ->
                        gameViewModel.placeOrRemoveShipAt(FleetRules.cellIndex(row, column))
                    },
                    onClearPlacementClick = gameViewModel::clearPlacement,
                    onPlacementErrorShown = gameViewModel::clearPlacementError,
                    onLobbyUnavailable = {
                        gameViewModel.clearLocalGame()
                        navController.navigateSingleTop(AppRoute.CreateJoin)
                    },
                    onShareCode = {
                        gameUiState.gameId.takeIf { it.isNotBlank() }?.let { gameId ->
                            shareGameCode(activity, clipboardManager, gameId)
                        }
                    }
                )
            }

            composable(AppRoute.Battle) {
                BackHandler {
                    battleBackAction()
                }

                BattleRoute(
                    appUiState = appUiState,
                    gameUiState = gameUiState,
                    onFireAtCell = { cellIndex ->
                        val currentUserId = appUiState.currentUserId ?: return@BattleRoute
                        val game = gameUiState.currentGame ?: return@BattleRoute
                        if (game.hostUid == currentUserId) {
                            gameViewModel.hostFire(currentUserId, cellIndex)
                        } else {
                            gameViewModel.guestFire(currentUserId, cellIndex)
                        }
                    },
                    onDismissGameResult = topBarBackAction,
                )
            }

            composable(AppRoute.History) {
                HistoryRoute(
                    historyUiState = historyUiState,
                    onRetry = {
                        appUiState.currentUserId?.let(historyViewModel::observeHistory)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String?,
    canGoBack: Boolean,
    onBackClick: () -> Unit,
) {
    val title = when (currentRoute) {
        AppRoute.Auth -> "Authentication"
        AppRoute.Profile -> "Profile"
        AppRoute.CreateJoin -> "Create / Join"
        AppRoute.Lobby -> "Lobby"
        AppRoute.Battle -> "Battle"
        AppRoute.History -> "History"
        else -> "Sea Battle"
    }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canGoBack && currentRoute != AppRoute.Home && currentRoute != AppRoute.Auth) {
                TextButton(onClick = onBackClick) {
                    Text("Back")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

@Composable
private fun AuthRoute(
    appUiState: AppUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScreen(
        email = email,
        password = password,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onSignInClick = { onSignIn(email, password) },
        onRegisterClick = { onRegister(email, password) },
        isLoading = appUiState.isLoading,
        errorMessage = appUiState.errorMessage,
    )
}

@Composable
private fun FirebaseSetupRequiredScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(modifier = Modifier.padding(24.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Firebase is not configured",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Add app/google-services.json from Firebase Console first. I left the exact setup steps in FIREBASE_SETUP.md.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun HomeRoute(
    appUiState: AppUiState,
    gameUiState: GameUiState,
    onCreateJoinClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onResumeLobbyClick: () -> Unit,
    onShareLobbyCodeClick: () -> Unit,
) {
    HomeScreen(
        playerName = appUiState.profile?.nickname ?: "Player",
        activeLobbyCode = gameUiState.gameId.takeIf { it.isNotBlank() },
        onCreateJoinClick = onCreateJoinClick,
        onProfileClick = onProfileClick,
        onHistoryClick = onHistoryClick,
        onLogoutClick = onLogoutClick,
        onResumeLobbyClick = onResumeLobbyClick,
        onShareLobbyCodeClick = onShareLobbyCodeClick,
        isLoading = appUiState.isLoading || gameUiState.isLoading,
        errorMessage = gameUiState.errorMessage ?: appUiState.errorMessage,
    )
}

@Composable
private fun ProfileRoute(
    appUiState: AppUiState,
    historyUiState: HistoryUiState,
    onSaveProfile: (String, AvatarChoice) -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    var nickname by rememberSaveable(appUiState.profile?.nickname) {
        mutableStateOf(appUiState.profile?.nickname.orEmpty())
    }
    var selectedAvatar by rememberSaveable(appUiState.profile?.avatarChoice?.storageName) {
        mutableStateOf(appUiState.profile?.avatarChoice ?: AvatarChoice.CAPTAIN)
    }

    val avatarOptions = remember { AvatarChoice.entries }

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileScreen(
            playerName = nickname,
            selectedAvatar = selectedAvatar,
            avatarOptions = avatarOptions,
            gamesPlayed = historyUiState.totalGames,
            wins = historyUiState.wins,
            losses = historyUiState.losses,
            onNameChange = { nickname = it },
            onAvatarSelected = { selectedAvatar = it },
            onSaveClick = {
                onSaveProfile(nickname, selectedAvatar)
            },
            isSaving = appUiState.isLoading,
            errorMessage = appUiState.errorMessage,
        )

        val notificationsEnabled = appUiState.notificationsEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        AppStateCard(
            title = "Notifications",
            message = if (notificationsEnabled) {
                "Notifications are enabled."
            } else {
                "Notifications are disabled. The game still works, but you may miss your turn reminders."
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            actionLabel = if (notificationsEnabled) null else "Allow",
            onActionClick = if (notificationsEnabled) null else onRequestNotifications,
        )

        if (!notificationsEnabled) {
            TextButton(
                onClick = onOpenNotificationSettings,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text("Open notification settings")
            }
        }
    }
}

@Composable
private fun CreateJoinRoute(
    appUiState: AppUiState,
    gameUiState: GameUiState,
    onCreateLobby: () -> Unit,
    onJoinLobby: (String) -> Unit,
) {
    var joinCode by rememberSaveable { mutableStateOf("") }

    CreateJoinScreen(
        playerName = appUiState.profile?.nickname ?: "Player",
        joinCode = joinCode,
        onJoinCodeChange = { joinCode = it.uppercase() },
        onCreateLobbyClick = onCreateLobby,
        onJoinLobbyClick = { onJoinLobby(joinCode) },
        isLoading = gameUiState.isLoading,
        errorMessage = gameUiState.errorMessage,
    )
}

@Composable
private fun LobbyRoute(
    appUiState: AppUiState,
    gameUiState: GameUiState,
    onReadyButtonClick: () -> Unit,
    onSelectShipSize: (Int) -> Unit,
    onToggleOrientation: () -> Unit,
    onBoardCellClick: (Int, Int) -> Unit,
    onClearPlacementClick: () -> Unit,
    onPlacementErrorShown: () -> Unit,
    onLobbyUnavailable: () -> Unit,
    onShareCode: () -> Unit,
) {
    val context = LocalContext.current
    val currentUserId = appUiState.currentUserId.orEmpty()
    val currentProfile = appUiState.profile
    val game = gameUiState.currentGame

    LaunchedEffect(gameUiState.placementErrorMessage) {
        val message = gameUiState.placementErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        onPlacementErrorShown()
    }

    LaunchedEffect(currentProfile, game) {
        if (currentProfile == null || game == null) {
            onLobbyUnavailable()
        }
    }

    if (currentProfile == null || game == null) {
        return
    }

    LobbyScreen(
        lobbyCode = game.gameId,
        players = game.toLobbyPlayers(),
        currentPlayerName = currentProfile.nickname,
        isCurrentPlayerReady = game.isReadyFor(currentUserId),
        placedShips = gameUiState.localShips,
        remainingShipSizes = gameUiState.remainingShipSizes,
        selectedShipSize = gameUiState.selectedShipSize,
        shipOrientation = gameUiState.placementOrientation,
        onReadyButtonClick = onReadyButtonClick,
        onSelectShipSize = onSelectShipSize,
        onToggleOrientation = onToggleOrientation,
        onBoardCellClick = onBoardCellClick,
        onClearPlacementClick = onClearPlacementClick,
        onShareCodeClick = onShareCode,
        isCurrentPlayerHost = game.hostUid == currentUserId,
        canStart = game.canStartBattle,
        isStarting = gameUiState.isLoading,
        statusMessage = game.toLobbyStatusMessage(currentUserId),
        errorMessage = gameUiState.errorMessage,
    )
}

@Composable
private fun BattleRoute(
    appUiState: AppUiState,
    gameUiState: GameUiState,
    onFireAtCell: (Int) -> Unit,
    onDismissGameResult: () -> Unit,
) {
    val currentUserId = appUiState.currentUserId.orEmpty()
    val game = gameUiState.currentGame

    if (game == null) {
        EmptyState(
            title = "Battle unavailable",
            message = "Opponent disconnected",
            modifier = Modifier.padding(16.dp),
        )
        return
    }

    BattleScreen(
        ownBoard = game.toOwnBoard(currentUserId),
        enemyBoard = game.toEnemyBoard(currentUserId),
        players = game.toLobbyPlayers(),
        currentTurnText = game.toTurnText(currentUserId),
        statusMessage = game.toBattleStatusMessage(currentUserId),
        onEnemyCellClick = { row, column ->
            onFireAtCell(row * FleetRules.BOARD_SIZE + column)
        },
        selectedEnemyCell = gameUiState.selectedEnemyCell,
        isYourTurn = game.currentTurnUid == currentUserId && !game.isFinished && !gameUiState.isSubmittingGuestShot,
        isLoading = gameUiState.isLoading || gameUiState.isSubmittingGuestShot,
        errorMessage = gameUiState.errorMessage,
        showGameResultDialog = game.isFinished,
        gameResultTitle = if (game.winnerUid == currentUserId) "Victory" else "Defeat",
        gameResultMessage = if (game.winnerUid == currentUserId) {
            "You destroyed the enemy fleet. Return to the main menu."
        } else {
            "Your fleet has been destroyed. Return to the main menu."
        },
        onDismissGameResult = onDismissGameResult,
    )
}

@Composable
private fun HistoryRoute(
    historyUiState: HistoryUiState,
    onRetry: () -> Unit,
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    HistoryScreen(
        matches = historyUiState.games.map { summary ->
            HistoryMatchUi(
                id = summary.gameId,
                opponentName = summary.opponentName,
                resultLabel = if (summary.didWin) "Victory" else "Defeat",
                dateLabel = formatter.format(Date(summary.finishedAt)),
                turns = summary.totalTurns,
                isWin = summary.didWin,
            )
        },
        onMatchClick = {},
        isLoading = historyUiState.isLoading,
        errorMessage = historyUiState.errorMessage,
        onRetryClick = onRetry,
    )
}

private fun GameState.toLobbyPlayers(): List<LobbyPlayerUi> {
    val items = mutableListOf<LobbyPlayerUi>()
    hostProfile?.let {
        items += LobbyPlayerUi(
            name = it.nickname,
            avatar = it.avatarChoice,
            isReady = hostReady,
            isHost = true,
        )
    }
    guestProfile?.let {
        items += LobbyPlayerUi(
            name = it.nickname,
            avatar = it.avatarChoice,
            isReady = guestReady,
            isHost = false,
        )
    }
    return items
}

private fun GameState.isReadyFor(uid: String): Boolean {
    return if (uid == hostUid) hostReady else guestReady
}

private fun GameState.toLobbyStatusMessage(currentUserId: String): String {
    return when {
        guestUid.isNullOrBlank() -> "Share the game code and place your ships while you wait."
        !isReadyFor(currentUserId) -> "Place your ships and mark yourself ready."
        !canStartBattle -> "Waiting for the other player to get ready."
        else -> "Both players are ready. Battle starts automatically."
    }
}

private fun GameState.toBattleStatusMessage(currentUserId: String): String {
    return when {
        isFinished && winnerUid == currentUserId -> "You won this battle."
        isFinished -> "You lost this battle."
        currentTurnUid == currentUserId -> "Tap a cell on the enemy board. If you hit, you keep shooting."
        else -> "Wait for the opponent. Their turn ends when they miss."
    }
}

private fun GameState.toTurnText(currentUserId: String): String {
    return when {
        isFinished && winnerUid == currentUserId -> "Victory"
        isFinished -> "Defeat"
        currentTurnUid == currentUserId -> "Your turn"
        else -> "Opponent turn"
    }
}

private fun GameState.toOwnBoard(currentUserId: String): List<List<BoardCellState>> {
    val ownShips = if (currentUserId == hostUid) hostShips else guestShips
    val ownHitsReceived = if (currentUserId == hostUid) hostShotsReceived else guestShotsReceived
    val sunkCells = ownShips.sunkCells(ownHitsReceived)

    return buildBoard { cellIndex ->
        when {
            cellIndex in sunkCells -> BoardCellState.Sunk
            cellIndex in ownHitsReceived && ownShips.any { cellIndex in it.cells } -> BoardCellState.Hit
            cellIndex in ownHitsReceived -> BoardCellState.Miss
            ownShips.any { cellIndex in it.cells } -> BoardCellState.Ship
            else -> BoardCellState.Empty
        }
    }
}

private fun GameState.toEnemyBoard(currentUserId: String): List<List<BoardCellState>> {
    val shotsMade = if (currentUserId == hostUid) hostShotsMade else guestShotsMade
    val enemyShips = if (currentUserId == hostUid) guestShips else hostShips
    val sunkCells = enemyShips.sunkCells(shotsMade)

    return buildBoard { cellIndex ->
        when {
            cellIndex in sunkCells -> BoardCellState.Sunk
            cellIndex in shotsMade && enemyShips.any { cellIndex in it.cells } -> BoardCellState.Hit
            cellIndex in shotsMade -> BoardCellState.Miss
            else -> BoardCellState.Empty
        }
    }
}

private fun List<com.example.seabattle.model.Ship>.sunkCells(hits: List<Int>): Set<Int> {
    return this.filter { ship -> ship.cells.all(hits::contains) }
        .flatMap { it.cells }
        .toSet()
}

private fun buildBoard(cellStateProvider: (Int) -> BoardCellState): List<List<BoardCellState>> {
    return List(FleetRules.BOARD_SIZE) { row ->
        List(FleetRules.BOARD_SIZE) { column ->
            val cellIndex = row * FleetRules.BOARD_SIZE + column
            cellStateProvider(cellIndex)
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

private fun Activity.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

private fun shareGameCode(
    activity: Activity?,
    clipboardManager: ClipboardManager?,
    gameId: String,
) {
    clipboardManager?.setPrimaryClip(ClipData.newPlainText("Sea Battle game code", gameId))
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Join my Sea Battle game with code: $gameId")
    }
    activity?.startActivity(Intent.createChooser(shareIntent, "Share game code"))
}

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
            @Suppress("UNCHECKED_CAST")
            return create() as VM
        }
    }
}
