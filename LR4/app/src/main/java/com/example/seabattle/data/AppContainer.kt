package com.example.seabattle.data

import android.content.Context
import com.google.firebase.FirebaseApp

class AppContainer(context: Context) {

    val isFirebaseConfigured: Boolean = FirebaseApp.getApps(context).isNotEmpty()

    val authRepository: AuthRepository? = if (isFirebaseConfigured) AuthRepository() else null
    val profileRepository: ProfileRepository? = if (isFirebaseConfigured) ProfileRepository() else null
    val gameRepository: GameRepository? = if (isFirebaseConfigured) GameRepository() else null
}
