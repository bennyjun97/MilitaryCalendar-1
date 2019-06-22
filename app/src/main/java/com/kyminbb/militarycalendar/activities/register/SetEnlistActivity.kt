package com.kyminbb.militarycalendar.activities.register

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.User
import net.grandcentrix.tray.AppPreferences
import org.jetbrains.anko.toast

class SetEnlistActivity : AppCompatActivity() {

    private val prefs by lazy { AppPreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_enlist)

        var userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)

        // Show ToastMessage
        toast("${userInfo.affiliation} 이시군요!\n언제 입대하셨나요?")
    }
}
