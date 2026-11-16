
package com.inbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inbo.app.ui.theme.InboTheme

private val Background = Color(0xFFF7F7FB)
private val Ink = Color(0xFF202137)
private val Muted = Color(0xFF85869A)
private val Purple = Color(0xFF7165E8)
private val SoftPurple = Color(0xFFECEAFF)
private val CardWhite = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InboTheme {
                InboApp()
            }
        }
    }
}

@Composable
fun InboApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Inbox", "Tasks", "Calendar", "Settings")
    val symbols = listOf("✉", "✓", "▦", "⚙")

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite,
                contentColor = Purple
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Text(
                                text = symbols[index],
                                fontSize = 20.sp
                            )
                        },
                        label = { Text(tab) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple,
                            selectedTextColor = Purple,
                            indicatorColor = SoftPurple,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> InboxScreen()
                1 -> SimpleScreen("Tasks", "Your tasks will live here.")
                2 -> SimpleScreen("Calendar", "Your upcoming events will live here.")
                3 -> SimpleScreen("Settings", "Make Inbo work your way.")
            }
        }
    }
}

@Composable
fun InboxScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "inbo",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "LESS NOISE. MORE LIFE.",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                    color = Muted
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SoftPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = Purple, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(38.dp))

        Text(
            text = "YOUR INBOX",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = Purple
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your day,\nwithout the noise.",
            fontSize = 32.sp,
            lineHeight = 39.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "The important things, all in one place.",
            fontSize = 14.sp,
            color = Muted
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "IMPORTANT",
                value = "0",
                subtitle = "New updates",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "YOUR TASKS",
                value = "0",
                subtitle = "To complete",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent updates",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = "See all",
                fontSize = 13.sp,
                color = Purple,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardWhite
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(SoftPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", fontSize = 27.sp, color = Purple)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All caught up!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Important updates will appear here\nwhen Inbo starts processing your inbox.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Muted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "YOUR TIME MATTERS.",
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = Muted
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = Muted
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Muted
            )
        }
    }
}

@Composable
fun SimpleScreen(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "inbo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = Muted
        )
    }
}
