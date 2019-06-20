package com.kyminbb.militarycalendar.*

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson

class SetEnlistActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_enlist)

        var userInfo = Gson().fromJson(prefs.getString("userInfo", ""), utils.User::class.java)

        // Show ToastMessage
        toast("${userInfo.affiliation} 이시군요!\n언제 입대하셨나요?")
    }
}
