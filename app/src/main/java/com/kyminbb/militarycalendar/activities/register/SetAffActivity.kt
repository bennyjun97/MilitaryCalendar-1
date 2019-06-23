package com.kyminbb.militarycalendar.activities.register

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.User
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import kotlinx.android.synthetic.main.activity_set_aff.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SetAffActivity : AppCompatActivity() {

    private val affiliations = arrayOf("육군", "해군", "공군", "의무경찰", "사회복무요원", "해병대", "의무소방대", "해양의무경찰")

    private var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_aff)

        loadData()
        DynamicToast.makeSuccess(this, "${userInfo.name}님! 소속이 어떻게 되세요?").show()

        val buttons = arrayOf(
            buttonArmy,
            buttonNavy,
            buttonAir,
            buttonMarine,
            buttonPolice,
            buttonPublic,
            buttonSeapolice,
            buttonFire
        )

        // Save the affiliation for each selection.
        for ((index, value) in buttons.withIndex()) {
            value.setOnClickListener {
                saveData(index)

                // Transition to the next activity.
                startActivity<SetEnlistActivity>()
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }

        // Back to SetNameActivity if necessary.
        backAffButton.setOnClickListener {
            startActivity<SetNameActivity>()
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun saveData(index: Int) {
        userInfo.affiliation = affiliations[index]
        val jsonString = Gson().toJson(userInfo)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        prefs.edit().putString("userInfo", jsonString).apply()
    }
}