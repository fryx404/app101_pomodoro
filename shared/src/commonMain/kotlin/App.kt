import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.PomodoroRepository
import data.PomodoroRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

interface SoundPlayer {
    fun playPomodoroEndSound()
    fun playBreakEndSound()
}

class DummySoundPlayer : SoundPlayer {
    override fun playPomodoroEndSound() {
        // Do nothing
    }

    override fun playBreakEndSound() {
        // Do nothing
    }
}

enum class TimerMode(val duration: Int) {
    Pomodoro(15),
    ShortBreak(5),
    LongBreak(10)
}

private val DarkColors = darkColors(
    primary = Color(0xFFBB86FC), // Accent color
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212), // Dark background
    surface = Color(0xFF1E1E1E), // Darker surface for cards/elements
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White, // White text on dark background
    onSurface = Color.White,
    error = Color(0xFFCF6679)
)

@Composable
fun App(soundPlayer: SoundPlayer, pomodoroRepository: PomodoroRepository) {
    MaterialTheme(colors = DarkColors) {
        var currentMode by remember { mutableStateOf(TimerMode.Pomodoro) }
        var seconds by remember { mutableStateOf(currentMode.duration) }
        var isRunning by remember { mutableStateOf(false) }
        var dailyPomodoroCount by remember { mutableStateOf(0) }
        var allPomodoroCounts by remember { mutableStateOf(emptyMap<String, Int>()) }

        // Load daily pomodoro count on app start
        LaunchedEffect(Unit) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            dailyPomodoroCount = pomodoroRepository.getPomodoroCounts()[today] ?: 0
            allPomodoroCounts = pomodoroRepository.getPomodoroCounts()
        }

        // Save daily pomodoro count when it changes
        LaunchedEffect(dailyPomodoroCount) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            pomodoroRepository.savePomodoroCount(today, dailyPomodoroCount)
            allPomodoroCounts = pomodoroRepository.getPomodoroCounts()
        }

        fun changeMode(newMode: TimerMode) {
            currentMode = newMode
            seconds = newMode.duration
            isRunning = false
        }

        LaunchedEffect(isRunning, currentMode, seconds) {
            if (isRunning) {
                if (seconds > 0) {
                    delay(1000)
                    seconds--
                } else {
                    when (currentMode) {
                        TimerMode.Pomodoro -> {
                            dailyPomodoroCount++
                            soundPlayer.playPomodoroEndSound()
                            delay(1000) // 1 second delay after pomodoro ends
                            val nextMode = if (dailyPomodoroCount % 4 == 0) TimerMode.LongBreak else TimerMode.ShortBreak
                            changeMode(nextMode)
                            isRunning = true // Automatically start the break
                        }
                        TimerMode.ShortBreak, TimerMode.LongBreak -> {
                            soundPlayer.playBreakEndSound()
                            changeMode(TimerMode.Pomodoro)
                            // Stop after a break, user starts next pomodoro manually
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { changeMode(TimerMode.Pomodoro) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) { Text("Pomodoro", color = MaterialTheme.colors.onPrimary) }
                Button(
                    onClick = { changeMode(TimerMode.ShortBreak) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) { Text("Short Break", color = MaterialTheme.colors.onPrimary) }
                Button(
                    onClick = { changeMode(TimerMode.LongBreak) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) { Text("Long Break", color = MaterialTheme.colors.onPrimary) }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "%02d:%02d".format(seconds / 60, seconds % 60),
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Daily Pomodoros: $dailyPomodoroCount",
                fontSize = 18.sp,
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isRunning = true },
                    enabled = !isRunning,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("Start", color = MaterialTheme.colors.onSecondary)
                }
                Button(
                    onClick = { isRunning = false },
                    enabled = isRunning,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("Stop", color = MaterialTheme.colors.onSecondary)
                }
                Button(
                    onClick = {
                        isRunning = false
                        seconds = currentMode.duration
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("Reset", color = MaterialTheme.colors.onSecondary)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
            CalendarView(allPomodoroCounts)
        }
    }
}

@Composable
fun CalendarView(pomodoroCounts: Map<String, Int>) {
    var currentMonth by remember { mutableStateOf<LocalDate>(Clock.System.todayIn(TimeZone.currentSystemDefault())) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
            ) {
                Text("< Prev", color = MaterialTheme.colors.onSurface)
            }
            Text(
                text = "${currentMonth.year}年 ${currentMonth.month.name}月",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )
            Button(
                onClick = { currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
            ) {
                Text("Next >", color = MaterialTheme.colors.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            DayOfWeek.values().forEach { dayOfWeek ->
                Text(dayOfWeek.name.substring(0, 3), fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Calendar grid
        val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.monthNumber, 1)
        val daysInMonth = currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0)) // Leap year check
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 for Monday, 6 for Sunday

        val calendarDays = mutableListOf<LocalDate?>()

        // Add leading empty days
        for (i in 0 until firstDayOfWeek) {
            calendarDays.add(null)
        }

        // Add days of the month
        for (i in 1..daysInMonth) {
            calendarDays.add(LocalDate(currentMonth.year, currentMonth.monthNumber, i))
        }

        // Add trailing empty days to fill the grid (up to 6 weeks)
        val totalCells = 42 // 6 rows * 7 days
        while (calendarDays.size < totalCells) {
            calendarDays.add(null)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            calendarDays.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    week.forEach { day ->
                        val isToday = day == Clock.System.todayIn(TimeZone.currentSystemDefault())
                        val backgroundColor = if (isToday) MaterialTheme.colors.primary.copy(alpha = 0.3f) else MaterialTheme.colors.surface
                        val textColor = if (isToday) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface

                        Column(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .background(backgroundColor, RoundedCornerShape(8.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (day != null) {
                                Text(day.dayOfMonth.toString(), fontSize = 14.sp, color = textColor)
                                val count = pomodoroCounts[day.toString()] ?: 0
                                if (count > 0) {
                                    Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}