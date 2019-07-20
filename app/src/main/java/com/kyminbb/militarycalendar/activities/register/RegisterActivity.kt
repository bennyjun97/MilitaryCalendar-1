package com.kyminbb.militarycalendar.activities.register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.commit451.addendum.threetenabp.toLocalDate
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.main.HomeActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val SELECT_PICTURE = 1
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1000
    }

    //Initialize today's date
    private val today = Calendar.getInstance().toLocalDate()

    // Initialize the user info.
    private var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_register)

        val dateInputs = arrayOf(inputEnlist, privateDate, corporalDate, sergeantDate, inputEnd)

        loadData(dateInputs)

        profileImage.setOnClickListener {
            setProfileImage()
        }

        inputAffiliation.setOnClickListener {
            setAffiliation()
        }

        setDates(dateInputs)

        register.setOnClickListener {
            completeRegister()
        }

        reset.setOnClickListener {
            init()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startGalleryIntent()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) try {
            // Load profile image to view
            val mImageUri = intent!!.data
            Glide.with(this).load(mImageUri).into(profileImage)

            // Get persistent Uri permission so that it will be allowed to reload when you restart the device
            userInfo.profileImage = mImageUri!!.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Load the user info from SharedPreferences.
    private fun loadData(dateInputs: Array<Button>) {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.contains("userInfo")) {
            userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
            // Display the stored profile image.
            profileImage.setImageURI(Uri.parse(userInfo.profileImage))
            // Display the stored affiliation.
            inputAffiliation.text = userInfo.affiliation
            // Display the promotion dates.
            for (index in Dates.RANK2.ordinal until dateInputs.size) {
                dateInputs[index].text = formatDate(userInfo.promotionDates[index])
            }
        }
        // Display the stored name.
        inputName.hint = userInfo.name
        // when name is loaded from data into hint, its color will be black
        inputName.setHintTextColor(Color.BLACK)
        // Display the enlist date.
        inputEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
    }

    private fun setProfileImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다", "권한이 필요한 이유") {
                    yesButton {
                        ActivityCompat.requestPermissions(
                            this@RegisterActivity,
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
        val affiliations = resources.getStringArray(R.array.affiliations_string)
        val affiliationButtons = arrayOf(
            R.id.army,
            R.id.navy,
            R.id.airforce,
            R.id.marine,
            R.id.police,
            R.id.seapolice,
            R.id.agent,
            R.id.fire
        )

        val popupMenu = PopupMenu(this, inputAffiliation)
        popupMenu.menuInflater.inflate(R.menu.affiliation_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            userInfo.affiliation = affiliations[affiliationButtons.indexOf(it.itemId)]
            inputAffiliation.text = userInfo.affiliation

            // Update promotion dates corresponding to the affiliation.
            calcPromotionDates()
            updatePromotionViews()
            // Update rank names corresponding to the affiliation.
            ranksByAffiliation()
            true
        }
        popupMenu.show()
    }

    private fun setDates(dateInputs: Array<Button>) {
        for ((index, value) in dateInputs.withIndex()) {
            value.setOnClickListener {
                // Alert if the user adjusts promotion dates before selecting an affiliation.
                if (index != Dates.ENLIST.ordinal && isEmpty(inputAffiliation.text)) {
                    DynamicToast.makeError(this, "군별을 골라주세요!").show()
                    return@setOnClickListener
                }
                setDate(index)
            }
        }
    }

    private fun setDate(dateIndex: Int) {
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            userInfo.promotionDates[dateIndex] = LocalDate.of(year, month + 1, day)
            if (dateIndex == Dates.ENLIST.ordinal) {
                calcPromotionDates()
            }
            updatePromotionViews()
        }

        val dialog = SpinnerDatePickerDialogBuilder()
            .context(this)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(today.year + 4, 11, 31)
            .minDate(today.year - 5, 0, 1)
            .defaultDate(
                userInfo.promotionDates[dateIndex].year,
                userInfo.promotionDates[dateIndex].monthValue - 1,
                userInfo.promotionDates[dateIndex].dayOfMonth
            )
        dialog.build().show()
    }

    // Calculate promotion dates.
    //현재 계급 값 설정 및 다음 진급일 표시 기능
    private fun calcPromotionDates() {
        val enlist = userInfo.promotionDates[Dates.ENLIST.ordinal]
        //각각 일병, 상병, 병장 진급일 계산
        userInfo.promotionDates[Dates.RANK2.ordinal] = DateCalc.calcRank2(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK3.ordinal] = DateCalc.calcRank3(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK4.ordinal] = DateCalc.calcRank4(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.END.ordinal] = DateCalc.calcETS(enlist, userInfo.affiliation)
    }

    @SuppressLint("SetTextI18n")
    private fun ranksByAffiliation() {
        privateText.text = DateCalc.rankString(1, userInfo.affiliation) + " 진급일"
        corporalText.text = DateCalc.rankString(2, userInfo.affiliation) + " 진급일"
        sergeantText.text = DateCalc.rankString(3, userInfo.affiliation) + " 진급일"
    }

    private fun completeRegister() {
        when {
            // https://github.com/pranavpandey/dynamic-toasts
            isEmpty(inputName.text.toString()) && isEmpty(userInfo.name) -> return DynamicToast.makeError(
                this,
                "이름을 입력해주세요!"
            ).show()
            isEmpty(inputAffiliation.text) -> return DynamicToast.makeError(this, "군별을 골라주세요!").show()
            else -> {
                saveData()
                startActivity<HomeActivity>()
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        if (isEmpty(inputName.text.toString()))
            userInfo.name = inputName.hint.toString()
        else
            userInfo.name = inputName.text.toString()
        when {
            today.isBefore(userInfo.promotionDates[Dates.RANK2.ordinal]) -> userInfo.rank = 0
            today.isBefore(userInfo.promotionDates[Dates.RANK3.ordinal]) -> userInfo.rank = 1
            today.isBefore(userInfo.promotionDates[Dates.RANK4.ordinal]) -> userInfo.rank = 2
            else -> userInfo.rank = 3
        }

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        // Save the data into UserData class.
        val jsonString = Gson().toJson(userInfo)
        prefs.edit().putString("userInfo", jsonString).putBoolean("firstStart", false).apply()
    }

    private fun init() {
        when {
            // https://github.com/pranavpandey/dynamic-toasts
            isEmpty(inputName.text.toString()) -> return DynamicToast.makeError(this, "이름을 입력해주세요!").show()
            isEmpty(inputAffiliation.text) -> return DynamicToast.makeError(this, "군별을 골라주세요!").show()
            else -> {
                DynamicToast.makeError(this, "조정 사항이 입대일을 기준으로 초기화됩니다!").show()
                calcPromotionDates()
                updatePromotionViews()
            }
        }
    }

    private fun updatePromotionViews() {
        inputEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
        privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
        inputEnd.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
    }
}
