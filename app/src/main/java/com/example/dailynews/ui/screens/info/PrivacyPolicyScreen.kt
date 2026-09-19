package com.oxclub.dailynews.ui.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "Replace this placeholder with your actual privacy policy before publishing. " +
            "Describe data collected by the app, analytics if any, advertising/AdMob data processing, " +
            "third-party services, children's privacy, retention, contact details, and user rights."
        )
    }
}
