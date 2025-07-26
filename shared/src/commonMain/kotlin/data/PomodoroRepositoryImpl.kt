package data
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PomodoroData(val counts: Map<String, Int>)

class PomodoroRepositoryImpl : PomodoroRepository {
    private val file = File("pomodoro_counts.json")
    private var pomodoroCounts: MutableMap<String, Int> = mutableMapOf()

    init {
        if (file.exists()) {
            val jsonString = file.readText()
            if (jsonString.isNotEmpty()) {
                try {
                    pomodoroCounts = Json.decodeFromString<PomodoroData>(jsonString).counts.toMutableMap()
                } catch (e: Exception) {
                    println("Error reading pomodoro_counts.json: ${e.message}")
                }
            }
        }
    }

    override suspend fun savePomodoroCount(date: String, count: Int) {
        pomodoroCounts[date] = count
        file.writeText(Json.encodeToString(PomodoroData(pomodoroCounts)))
    }

    override suspend fun getPomodoroCounts(): Map<String, Int> {
        return pomodoroCounts.toMap()
    }
} 