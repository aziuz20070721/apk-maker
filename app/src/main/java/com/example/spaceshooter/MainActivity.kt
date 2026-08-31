package com.example.spaceshooter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.view.Gravity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "SpaceShooter\nHello, APK built!"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        setContentView(tv)
    }
}
