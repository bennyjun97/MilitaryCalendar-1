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

    companion object {
        const val affils = arrayOf("육군", "해군", "공군", "의무경찰", "사회복무요원", "해병대", "의무소방대", "해양의무경찰")
        const val buttons = arrayOf(
            buttonArmy,
            buttonNavy,
            buttonAir,
            buttonPolice,
            bottnPublic,
            buttonMarine,
            buttonFire,
            buttonSeapolice
        )
    }

    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_aff)

        var userInfo = Gson().fromJson(prefs.getString("userInfo", ""), utils.User::class.java)

        toast("${userInfo.name}님! 소속이 어떻게 되세요?")

        // Save the affiliation for each selection.
        for ((index, value) in buttons.withIndex()) {
            value.setOnClickListener {
                userInfo.affiliation = affils[index]
                val jsonString = Gson().toJson(userInfo)
                prefs.edit().putString("userInfo", jsonString).apply()
                startActivity<SetEnlistActivity>()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        // Back to SetNameActivity if necessary.
        backAffButton.setOnClickListener {
            startActivity<SetNameActivity>()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
