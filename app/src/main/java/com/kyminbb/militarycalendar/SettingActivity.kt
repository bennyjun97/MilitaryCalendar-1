package com.kyminbb.militarycalendar

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.commit451.addendum.threetenabp.toLocalDate
import com.jakewharton.threetenabp.AndroidThreeTen
import org.jetbrains.anko.startActivity
import java.util.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    // Initialize today's date.
    private val today = Calendar.getInstance().toLocalDate()
    private val todayYear = today.year
    private val todayMonth = today.monthValue - 1
    private val todayDay = today.dayOfMonth

    // Initialize the user info.
    var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_setting)
        //
        // Load the user info if there exists.
        loadData()

        // Update profile image.
        profileImage.setOnClickListener {
            setProfileImage()
        }

        // Update the enlist date.
        inputEnlistDate.setOnClickListener {
            setEnlistDate()
        }

        // Update the affiliation.
        inputAffiliation.setOnClickListener {
            setAffiliation()
        }

        // Update the promotion dates.
        inputPromotionDates.setOnClickListener {
            setPromotionDates()
        }

        // Complete the info update and save.
        buttonComplete.setOnClickListener {
            saveData()
            // Transition to the main page.
            startActivity<MainActivity>()
        }

        // Initialize the user info and the activity.
        buttonInit.setOnClickListener { }
    }

    // Load the user info from SharedPreferences.
    private fun loadData() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)
        // Load if the application is not first-time executed.
        if (!firstStart) { }
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false).apply()
    }

    private fun setProfileImage() {
        // First check whether permission to read gallery is granted.

        // Start gallery intent if allowed.

        // Update the profile image with the loaded image file.

    }

    private fun setEnlistDate() {
        // Use SpinnerDatePicker to select the enlist date.
        // 여기 참고 -> https://github.com/drawers/SpinnerDatePicker

    }

    private fun setAffiliation() { }

    private fun setPromotionDates() { }
}
