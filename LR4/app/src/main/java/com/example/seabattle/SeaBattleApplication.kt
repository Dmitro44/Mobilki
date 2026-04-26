package com.example.seabattle

import android.app.Application
import com.example.seabattle.data.AppContainer

class SeaBattleApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
