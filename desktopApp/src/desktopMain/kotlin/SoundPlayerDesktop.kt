package main

import App
import SoundPlayer
import javax.sound.sampled.AudioSystem
import java.io.BufferedInputStream

class SoundPlayerDesktop : SoundPlayer {
    override fun playPomodoroEndSound() {
        try {
            val audioInputStream = AudioSystem.getAudioInputStream(BufferedInputStream(javaClass.classLoader.getResourceAsStream("pomodoro_end.wav")))
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
        } catch (e: Exception) {
            println("Error playing pomodoro end sound: ${e.message}")
        }
    }

    override fun playBreakEndSound() {
        try {
            val audioInputStream = AudioSystem.getAudioInputStream(BufferedInputStream(javaClass.classLoader.getResourceAsStream("break_end.wav")))
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
        } catch (e: Exception) {
            println("Error playing break end sound: ${e.message}")
        }
    }
}
