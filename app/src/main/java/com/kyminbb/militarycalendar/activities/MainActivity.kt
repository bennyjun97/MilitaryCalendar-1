package com.kyminbb.militarycalendar.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.register.SetNameActivity
import com.kyminbb.militarycalendar.utils.ClockView
import com.kyminbb.militarycalendar.utils.User
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        Toast.makeText(this, userInfo.name, Toast.LENGTH_SHORT).show()
        val firstStart = prefs.getBoolean("firstStart", true)

        // Open the sign-up page if the application is first-time executed.
        if (firstStart) {
            startActivity<SetNameActivity>()
        } else {
            startActivity<GraphActivity>()
        }
    }
}
