package com.kyminbb.militarycalendar.activities.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.User
import org.jetbrains.anko.toast

class SetEnlistActivity : AppCompatActivity() {

    private var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_enlist)

        loadData()

        // Show ToastMessage
        toast("${userInfo.affiliation} 이시군요!\n언제 입대하셨나요?")
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}
