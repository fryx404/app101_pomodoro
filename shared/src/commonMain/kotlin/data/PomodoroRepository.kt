package data

interface PomodoroRepository {
    suspend fun savePomodoroCount(date: String, count: Int)
    suspend fun getPomodoroCounts(): Map<String, Int>
}