package com.kyminbb.militarycalendar.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.register.SetNameActivity
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        // Open the sign-up page if the application is first-time executed.
        // if (firstStart) { }
        startActivity<SetNameActivity>()
    }
}
