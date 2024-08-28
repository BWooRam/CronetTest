package com.geekstudio.cronettest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geekstudio.cronettest.ui.theme.CronetTestTheme


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CronetTestTheme {
                Column {
                    Spacer(modifier = Modifier.fillMaxWidth().height(100.dp))
                    Button(onClick = {
                        startActivity(Intent(this@MainActivity, CronetActivity::class.java))
                    }) {
                        Text(text = "Cronet 테스트 페이지")
                    }
                    Button(onClick = {
                        startActivity(Intent(this@MainActivity, OkhttpAndCronetActivity::class.java))
                    }) {
                        Text(text = "Okhttp + Cronet 테스트 페이지")
                    }
                }
            }
        }
    }
}