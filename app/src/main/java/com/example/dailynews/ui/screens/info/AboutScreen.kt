package com.oxclub.dailynews.ui.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("About Daily News", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Daily News is a simple news reader that displays headlines from a licensed news data provider and links to original publishers.")
    }
}
