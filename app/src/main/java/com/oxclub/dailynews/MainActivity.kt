package com.oxclub.dailynews
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.oxclub.dailynews.ui.DailyNewsAppUi
class MainActivity: ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{DailyNewsAppUi()}}}
