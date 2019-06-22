package com.kyminbb.militarycalendar.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.register.SetNameActivity
import net.grandcentrix.tray.AppPreferences
import org.jetbrains.anko.internals.AnkoInternals.getContext
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        // https://github.com/grandcentrix/tray
        val prefs = AppPreferences(applicationContext)
        val firstStart = prefs.getBoolean("firstStart", true)

        //startActivity<TabActivity>()

        // Open the sign-up page if the application is first-time executed.
        // if (firststart)이어야 하나 테스트를 위해 if(true)로 임시 설정함
        if (true) {
            startActivity<SetNameActivity>()
            //startActivity<SettingNameActivity>()
        }
    }
}
