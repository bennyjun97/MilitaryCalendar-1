package com.kyminbb.militarycalendar.activities.register

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.User
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import kotlinx.android.synthetic.main.activity_set_name.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SetNameActivity : AppCompatActivity() {

    // Initialize the user info.
    private var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_name)

        loadData()

        // Toast 메세지로 환영인사를 한다
        // 이름값을 입력 받는다
        // 넥스트 버튼을 눌러서 다음 화면으로 넘어가게 하는 intent값을 부여한다
        // 만약 이름이 없다면 toast 메세지를 띄워서 이름을 입력하도록 하게 한다.
        // 이름이 있다면 다음화면으로 넘어가게 한다.

        nextNameButton.setOnClickListener {
            /* when there is no name input returns a toast message
            if there is input, save name in the User() data class
            when the name is saved, create an intent so that the activity moves onto the next class (SettingAffiliationActivity)
              */

            if (TextUtils.isEmpty(nameText.text.toString())) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "이름을 입력해주세요!").show()
                return@setOnClickListener
            } else {
                saveData()
                // Transition to the next activity.
                startActivity<SetAffActivity>()
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.contains("userInfo")) {
            userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
        }
    }

    private fun saveData() {
        userInfo.name = nameText.text.toString()
        val jsonString = Gson().toJson(userInfo)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        prefs.edit().putString("userInfo", jsonString).apply()
    }
}
