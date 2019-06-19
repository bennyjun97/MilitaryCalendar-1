package com.kyminbb.militarycalendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)

        startActivity<TabActivity>()

        // Open the sign-up page if the application is first-time executed.
        // if (firststart)이어야 하나 테스트를 위해 if(true)로 임시 설정함
        if (false) {
            startActivity<SetNameActivity>()
            //startActivity<SettingNameActivity>()
        }
    }
}
