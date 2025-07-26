package main

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import App
import main.SoundPlayerDesktop
import data.PomodoroRepositoryImpl

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App(SoundPlayerDesktop(), PomodoroRepositoryImpl())
    }
}