package com.kyminbb.militarycalendar

import android.Manifest
import android.app.Activity
import android.content.Context
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

    // Initialize SharedPreference after the activity is initialized.
    private val prefs by lazy { getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_setting)

        // Initialize the view.
        init()

        // Load the user info if there exists.
        loadData()

        // Update profile image.
        buttonProfileImage.setOnClickListener {
            setProfileImage()
        }

        // Update the affiliation.
        setAffiliation()

        // Change the ETS date only when automatically added date is inaccurate.
        inputEnlistDate.setOnClickListener {
            setDate("Enlist")
        }

        // Update the end date.
        inputEndDate.setOnClickListener {
            setDate("End")
        }

        // Update the promotion dates.
        inputPromotionDate.setOnClickListener {
            setPromotionDates()
        }

        // Complete the info update and save.
        buttonComplete.setOnClickListener {
            saveData()
            // Transition to the main page.
            startActivity<MainActivity>()
        }

        // Initialize the user info and the activity.
        buttonInit.setOnClickListener {
            init()
        }
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

    private fun init() {
        buttonProfileImage.setImageResource(R.drawable.profile)
        inputName.text.clear()
        inputEnlistDate.text = "${todayYear}/${todayMonth}/${todayDay}"
        inputEndDate.text = "전역일"
        inputPromotionDate.text = "진급일"
    }

    // Load the user info from SharedPreferences.
    private fun loadData() {
        val firstStart = prefs.getBoolean("firstStart", true)
        // Load if the application is not first-time executed.
        if (!firstStart) {

            // load datas including name, affiliation, enlistdate, enddate, promotion date
            // load from the User.kt (data class)
            // data would be saved as JSon String
            inputName.text = prefs.getString(userInfo.name, "")
            inputAffiliation.whatever = prefs.getString(userInfo.affiliation, "")
            inputEnlistDate.text = prefs.getString(userInfo.promotionDates[Dates.ENLIST.ordinal].toString(), "")
            inputEndDate.text = prefs.getString(userInfo.promotionDates[Dates.END.ordinal].toString(), "")
            inputPromotionDate.text = prefs.getString(userInfo.promotionDates[Dates.needfunction].toString(), "")
        }
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {

        // save the data into UserData Class
        userInfo.name = inputName.text.toString()
        // convert the value of recylerView to string
        userInfo.affiliation = inputAffiliation.text.toString()
        userInfo.profileImage = buttonProfileImage.toString()
        // calcuate rank based on dateCalc class
        userInfo.rank = 1
        // calcuate promotion date based on dateCalc class
        // userInfo.promotionDates =
        val editor = prefs.edit()

        // create a jsonString to save data as string
        // jsonString would look like {"name" : "", "affiliation" : "", profileImage : "", rank : int, promotionDate : MutableList }
        val jsonString = Gson().toJson(userInfo)


        editor.putString("userInfo", jsonString)
            .putBoolean("firstStart", false).apply()
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
                    inputEnlistDate.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
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
                    dialog.defaultDate(userInfo.promotionDates[Dates.END.ordinal].year,
                        userInfo.promotionDates[Dates.END.ordinal].monthValue - 1,
                        userInfo.promotionDates[Dates.END.ordinal].dayOfMonth)
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
        var enlist = userInfo.promotionDates[Dates.ENLIST.ordinal]
        //각각 일병, 상병, 병장 진급일 계산
        userInfo.promotionDates[Dates.RANK2.ordinal] = DateCalc.calcRank2(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK3.ordinal] = DateCalc.calcRank3(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK4.ordinal] = DateCalc.calcRank4(enlist, userInfo.affiliation)

        //현재 날짜와 비교해서 현재 계급 값 설정, 다음 진급일 표시
        if(today.isBefore(userInfo.promotionDates[Dates.RANK2.ordinal])) {
            userInfo.rank = 0
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        }
        else if(today.isBefore(userInfo.promotionDates[Dates.RANK3.ordinal])) {
            userInfo.rank = 1
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        }
        else if(today.isBefore(userInfo.promotionDates[Dates.RANK4.ordinal])) {
            userInfo.rank = 2
            inputPromotionDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
        }
        else {
            userInfo.rank = 3
        }
    }

    private fun setPromotionDates() {

    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY/MM/dd"))
    }
}