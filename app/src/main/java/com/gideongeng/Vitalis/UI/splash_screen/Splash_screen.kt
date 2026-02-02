package com.gideongeng.Vitalis.UI.splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gideongeng.Vitalis.R
import com.gideongeng.Vitalis.UI.Auth.MainAuthentication
import com.gideongeng.Vitalis.UI.Home.Home_screen
import com.google.firebase.auth.FirebaseAuth

class Splash_screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logoImg: ImageView = findViewById(R.id.logo_img)
        val titleTxt: TextView = findViewById(R.id.title)
        val taglineTxt: TextView = findViewById(R.id.tagline)

        // Intro Animations (Scale and Fade)
        logoImg.alpha = 0f
        logoImg.scaleX = 0.5f
        logoImg.scaleY = 0.5f
        titleTxt.alpha = 0f
        taglineTxt.alpha = 0f

        logoImg.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        titleTxt.animate()
            .alpha(1f)
            .setStartDelay(600)
            .setDuration(800)
            .start()

        taglineTxt.animate()
            .alpha(1f)
            .setStartDelay(1000)
            .setDuration(800)
            .start()

        Handler().postDelayed({
            val fAuth: FirebaseAuth = FirebaseAuth.getInstance()
            val user = fAuth.currentUser
            if (user != null) {
                user.reload().addOnCompleteListener {
                    if (user.isEmailVerified) {
                        startActivity(Intent(this, Home_screen::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, MainAuthentication::class.java))
                        finish()
                    }
                }
            } else {
                startActivity(Intent(this, MainAuthentication::class.java))
                finish()
            }
        }, 3500) // Longer delay for premium feel
    }
}
