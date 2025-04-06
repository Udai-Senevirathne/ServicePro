package com.nibm.mynewapp

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Find the logo ImageView
        val splashLogo: ImageView = findViewById(R.id.splash_logo)

        // Load and start the fade + zoom animation
        val fadeZoomAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_zooom)
        splashLogo.startAnimation(fadeZoomAnimation)

        // Navigate to LoginScreen after a delay
        splashLogo.postDelayed({
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            // Add a fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish() // Close SplashScreen
        }, 2500) // 2.5 seconds delay
    }
}