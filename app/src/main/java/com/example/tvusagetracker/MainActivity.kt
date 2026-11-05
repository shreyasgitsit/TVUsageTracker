
package com.example.tvusagetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.example.tvusagetracker.ui.theme.TVUsageTrackerTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val Background = Color(0xFF0B1020)
private val Panel = Color(0xFF151E32)
private val Muted = Color(0xFF929FB8)
private val White = Color(0xFFF4F7FF)
private val Blue = Color(0xFF7295FF)
private val Green = Color(0xFF54D6B0)
private val Red = Color(0xFFFF7777)
private val FocusColor = Color(0xFFFFD166)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usageTracker = UsageTracker(this)

        setContent {
            TVUsageTrackerTheme {
                Dashboard(usageTracker)
            }
        }
    }
}

@Composable
fun Dashboard(usageTracker: UsageTracker) {

    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedSource by remember { mutableStateOf("None") }

    var todayUsage by remember {
        mutableStateOf(UsageTracker.DailyUsage(0L, 0L))
    }

    LaunchedEffect(usageTracker) {
        todayUsage = usageTracker.getTodayUsage()
    }

    val displayedMonth = remember(monthOffset) {
        YearMonth.now()
            .plusMonths(monthOffset.toLong())
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    fun formatTime(minutes: Long): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return "%02dh %02dm".format(hours, remainingMinutes)
    }

    val youtubeTime = todayUsage.youtubeMinutes
    val smartTubeTime = todayUsage.smartTubeMinutes
    val todayTotal = youtubeTime + smartTubeTime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // HEADER

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "TV USAGE TRACKER",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "YOUR VIEWING ACTIVITY",
                    color = Muted,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }

            Text(
                "LOCAL DASHBOARD",
                color = Green,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // MONTHLY OVERVIEW

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .background(Panel, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {

                Text(
                    "MONTHLY VIEWING TIME",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    RemoteButton(
                        label = "‹",
                        enabled = monthOffset > -24
                    ) {
                        monthOffset--
                    }

                    Text(
                        displayedMonth,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    RemoteButton(
                        label = "›",
                        enabled = monthOffset < 0
                    ) {
                        monthOffset++
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "--h --m",
                    color = White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "Monthly total is being prepared",
                    color = Muted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Today's tracked time: ${formatTime(todayTotal)}",
                    color = Blue,
                    fontSize = 13.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .background(Panel, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {

                Text(
                    "MONTHLY COMPARISON",
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "--",
                    color = White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Previous month",
                    color = Muted,
                    fontSize = 14.sp
                )
            }
        }

        // VIEWING SOURCES

        Text(
            "VIEWING SOURCES",
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            SourceCard(
                "YOUTUBE",
                formatTime(youtubeTime),
                "Today's app usage",
                Blue,
                selectedSource == "YouTube",
                Modifier.weight(1f)
            ) {
                selectedSource = "YouTube"
            }

            SourceCard(
                "SMARTTUBE",
                formatTime(smartTubeTime),
                "Today's app usage",
                Green,
                selectedSource == "SmartTube",
                Modifier.weight(1f)
            ) {
                selectedSource = "SmartTube"
            }

            SourceCard(
                "PS5",
                "--h --m",
                "HDMI 2 · Not connected yet",
                Red,
                selectedSource == "PS5",
                Modifier.weight(1f)
            ) {
                selectedSource = "PS5"
            }
        }

        if (selectedSource != "None") {
            Text(
                "Selected: $selectedSource",
                color = FocusColor,
                fontSize = 13.sp
            )
        }

        // VIEWING TRENDS

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Panel, RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {

            Text(
                "VIEWING TRENDS",
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "DAILY ACTIVITY · $displayedMonth",
                color = Muted,
                fontSize = 11.sp
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Historical viewing data will appear here",
                    color = Muted,
                    fontSize = 14.sp
                )
            }
        }

        Text(
            "LOCAL TRACKING  ·  NO CLOUD ACCOUNT REQUIRED",
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun RemoteButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Panel, RoundedCornerShape(10.dp))
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) FocusColor
                else Muted.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) White
            else Muted.copy(alpha = 0.35f),
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SourceCard(
    name: String,
    time: String,
    subtitle: String,
    accent: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .height(145.dp)
            .background(Panel, RoundedCornerShape(18.dp))
            .border(
                width = if (focused || selected) 3.dp else 1.dp,
                color = when {
                    focused -> FocusColor
                    selected -> accent
                    else -> Panel
                },
                shape = RoundedCornerShape(18.dp)
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .clickable(onClick = onClick)
            .focusable()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .width(34.dp)
                .height(4.dp)
                .background(accent, RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.height(13.dp))

        Text(
            name,
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            time,
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            subtitle,
            color = Muted,
            fontSize = 12.sp
        )
    }
}
