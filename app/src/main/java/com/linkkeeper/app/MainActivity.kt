package com.linkkeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = LinkStore(applicationContext)
        val auth = PasswordStore(applicationContext)
        setContent {
            App(store, auth, onExit = { finishAndRemoveTask() })
        }
    }
}

@Composable
fun App(store: LinkStore, auth: PasswordStore, onExit: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF2F5BEA),
                background = Color(0xFFF6F5F1),
                surface = Color.White,
            )
        ) {
            var unlocked by remember { mutableStateOf(false) }
            if (!unlocked) {
                LockScreen(auth) { unlocked = true }
            } else {
                LinksApp(store, onExit)
            }
        }
    }
}
