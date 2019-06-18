package com.kyminbb.militarycalendar

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_set_name.*
import org.jetbrains.anko.startActivity

class SetNameActivity : AppCompatActivity() {

    // Create a shared preference 객체, settingName 하고나서 사용되기에 lazy 위임
    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_name)

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

            if (TextUtils.isEmpty(nameText.text.toString())){
                Toast.makeText(applicationContext, "이름을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                userInfo.name = nameText.text.toString()
                val jsonString = Gson().toJson(userInfo)
                prefs.edit().putString("userInfo", jsonString).apply()
            }
            startActivity<SetAffActivity>()
        }
    }
}
