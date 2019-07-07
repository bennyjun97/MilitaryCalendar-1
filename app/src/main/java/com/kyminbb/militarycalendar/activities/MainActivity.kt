package com.kyminbb.militarycalendar.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.main.HomeActivity
import com.kyminbb.militarycalendar.activities.register.SetNameActivity
import com.kyminbb.militarycalendar.utils.ClockView
import com.kyminbb.militarycalendar.utils.User
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        // Open the sign-up page if the application is first-time executed.
        // 실험할 때는 느낌표를 놓아서 무조건 SetName 들어가게 하지만 나중에는 지울것!
        if (!firstStart) {
            startActivity<SetNameActivity>()
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        } else {
            startActivity<HomeActivity>()
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
    }
    
    //When the splash screen is on, pressing the back button will not have any effect.
    //나중에 주석 지울 것
   // override fun onBackPressed() {}
}
