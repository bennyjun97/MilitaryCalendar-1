package com.kyminbb.militarycalendar.activities.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.commit451.addendum.threetenabp.toLocalDate
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.ClockActivity
import com.kyminbb.militarycalendar.activities.MainActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
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
    private val todayMonth = today.monthValue
    private val todayDay = today.dayOfMonth

    // Initialize the user info.
    var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        setContentView(R.layout.activity_setting)

        // Load the user info if there exists.
        loadData()

        buttonComplete.setOnClickListener {
            saveData()
            startActivity<ClockActivity>()
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        buttonInit.setOnClickListener {
            startActivity<SetNameActivity>()
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
    }
/*
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
*/
    // Load the user info from SharedPreferences.
    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
        setFinalName.text = userInfo.name
        setFinalAff.text = userInfo.affiliation
        setFinalEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        // save the data into UserData Class
        val jsonString = Gson().toJson(userInfo)
        prefs.edit().putString("userInfo", jsonString).putBoolean("firstStart", false).apply()
    }
/*
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
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE
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
        intent.type = "image"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select image"),
            SELECT_PICTURE
        )
    }

    private fun setAffiliation() {
        // Use spinner to select the affiliation.
        val affiliations = arrayOf("육군/의경", "해군/해양의무경찰", "공군", "해병", "사회복무요원", "의무소방")
        inputAffiliation.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, affiliations)
        inputAffiliation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Affiliations[position] is the string of user's affiliation
                userInfo.affiliation = affiliations[position]
            }
        }
    }

    private fun setDate(type: String) {
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        var dateSetListener = DatePickerDialog.OnDateSetListener { _, _, _, _ -> }
        when (type) {
            "Enlist" -> {
                dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    userInfo.promotionDates[Dates.ENLIST.ordinal] = LocalDate.of(year, month + 1, day)
                    setFinalEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
                    calcEndDate()
                    calcPromotionDates()
                }
            }
            "End" -> {
                dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    userInfo.promotionDates[Dates.END.ordinal] = LocalDate.of(year, month + 1, day)
                    inputEndDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
                }
            }
        }

        val dialog = SpinnerDatePickerDialogBuilder()
            .context(this)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(todayYear + 4, 11, 31)
            .minDate(todayYear - 5, 0, 1)
        when (type) {
            "Enlist" -> {
                if (userInfo.promotionDates.isEmpty()) {
                    dialog.defaultDate(todayYear, todayMonth, todayDay)
                } else {
                    dialog.defaultDate(
                        userInfo.promotionDates[Dates.ENLIST.ordinal].year,
                        userInfo.promotionDates[Dates.ENLIST.ordinal].monthValue - 1,
                        userInfo.promotionDates[Dates.ENLIST.ordinal].dayOfMonth
                    )
                }
            }
            "End" -> {
                if (userInfo.promotionDates.isNotEmpty()) {
                    dialog.defaultDate(
                        userInfo.promotionDates[Dates.END.ordinal].year,
                        userInfo.promotionDates[Dates.END.ordinal].monthValue - 1,
                        userInfo.promotionDates[Dates.END.ordinal].dayOfMonth
                    )
                }
            }
        }
        dialog.build().show()
    }

    private fun calcEndDate() {
        //call calcETS method of dateCalc to calculate ETS date
        userInfo.promotionDates[Dates.END.ordinal] =
            DateCalc.calcETS(userInfo.promotionDates[Dates.ENLIST.ordinal], userInfo.affiliation)
        inputEndDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
    }

    //일병, 상병, 병장 진급일 계산
    //현재 계급 값 설정 및 다음 진급일 표시 기능
    private fun calcPromotionDates() {
        val enlist = userInfo.promotionDates[Dates.ENLIST.ordinal]
        //각각 일병, 상병, 병장 진급일 계산
        userInfo.promotionDates[Dates.RANK2.ordinal] = DateCalc.calcRank2(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK3.ordinal] = DateCalc.calcRank3(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK4.ordinal] = DateCalc.calcRank4(enlist, userInfo.affiliation)

        //현재 날짜와 비교해서 현재 계급 값 설정, 다음 진급일 표시
        if (today.isBefore(userInfo.promotionDates[Dates.RANK2.ordinal])) {
            userInfo.rank = 0
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        } else if (today.isBefore(userInfo.promotionDates[Dates.RANK3.ordinal])) {
            userInfo.rank = 1
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        } else if (today.isBefore(userInfo.promotionDates[Dates.RANK4.ordinal])) {
            userInfo.rank = 2
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
        } else {
            userInfo.rank = 3
        }
    }

    private fun setPromotionDates() {

    }
*/
    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
    }
}
