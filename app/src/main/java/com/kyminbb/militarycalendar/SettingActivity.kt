package com.kyminbb.militarycalendar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.commit451.addendum.threetenabp.toLocalDate
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import java.io.IOException
import java.util.*

class SettingActivity : AppCompatActivity() {

    companion object {
        const val SELECT_PICTURE = 1
        const val REQUEST_READ_EXTERNAL_STORAGE = 1000
    }

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
        buttonProfileImage.setOnClickListener {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Start gallery intent if permission is granted.
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                startGalleryIntent()
                /*if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryIntent()
                }*/
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) try {
            // Update the profile image with the loaded image file.
            val mImageUri = intent!!.data
            Glide.with(this).load(mImageUri).into(buttonProfileImage)

            // Get persistent Uri permission so that it will be allowed to reload when you restart the application.
            userInfo.profileImage = mImageUri!!.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Load the user info from SharedPreferences.
    private fun loadData() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStart = prefs.getBoolean("firstStart", true)
        // Load if the application is not first-time executed.
        if (!firstStart) {
        }
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false).apply()
    }

    private fun setProfileImage() {
        // First check whether permission to read gallery is granted.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Request permission to user.
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다", "권한이 필요한 이유") {
                    yesButton {
                        ActivityCompat.requestPermissions(
                            this@SettingActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                    noButton { }
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            // Start gallery intent if permission is granted.
            startGalleryIntent()
        }
    }

    private fun startGalleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }

    private fun setEnlistDate() {
        // Use SpinnerDatePicker to select the enlist date.
        // 여기 참고 -> https://github.com/drawers/SpinnerDatePicker

    }

    private fun setAffiliation() {
        // Use PopupMenu to show the list of affiliations to choose from.
    }

    private fun setPromotionDates() {}


}
