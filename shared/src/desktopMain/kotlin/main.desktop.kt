import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import data.PomodoroRepository

// actual fun getPlatformName(): String = "Desktop"
fun getPlatformName(): String = "Desktop"

class DummyPomodoroRepository : PomodoroRepository {
    override suspend fun savePomodoroCount(date: String, count: Int) {
        // Do nothing
    }

    override suspend fun getPomodoroCounts(): Map<String, Int> {
        return emptyMap()
    }
}

@Preview
@Composable
fun AppPreview() {
    App(DummySoundPlayer(), DummyPomodoroRepository())
}