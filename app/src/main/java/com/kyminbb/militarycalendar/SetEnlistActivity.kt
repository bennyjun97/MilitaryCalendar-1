package com.kyminbb.militarycalendar

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson

class SetEnlistActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_enlist)

        var userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)

        // Show ToastMessage
        Toast.makeText(applicationContext, "${userInfo.affiliation} ${userInfo.name}님! 언제 입대하셨나요?", Toast.LENGTH_SHORT).show()
    }
}
