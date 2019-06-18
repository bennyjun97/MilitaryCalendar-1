package com.kyminbb.militarycalendar

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_set_aff.*
import kotlinx.android.synthetic.main.activity_set_name.*
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.startActivity

class SetAffActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_aff)

        var userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)

        // Show ToastMessage
        Toast.makeText(applicationContext, "${userInfo.name}님! 소속이 어떻게 되세요?", Toast.LENGTH_SHORT).show()

        // save affiliation

        buttonArmy.setOnClickListener {
            userInfo.affiliation = "육군"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonNavy.setOnClickListener {
            userInfo.affiliation = "해군"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonAir.setOnClickListener {
            userInfo.affiliation = "공군"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonPolice.setOnClickListener {
            userInfo.affiliation = "의경"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonPublic.setOnClickListener {
            userInfo.affiliation = "사회복무요원"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonMarine.setOnClickListener {
            userInfo.affiliation = "해병대"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonFire.setOnClickListener {
            userInfo.affiliation = "의방"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        buttonSeapolice.setOnClickListener {
            userInfo.affiliation = "해양의무경찰"
            val jsonString = Gson().toJson(userInfo)
            prefs.edit().putString("userInfo", jsonString).apply()
            startActivity<SetEnlistActivity>()
        }

        // back button, reset name
        backAffButton.setOnClickListener {
            startActivity<SetNameActivity>()
        }
    }
}
