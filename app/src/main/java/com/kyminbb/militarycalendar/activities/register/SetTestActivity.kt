package com.kyminbb.militarycalendar.activities.register

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_set_test.*
import org.jetbrains.anko.startActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class SetTestActivity : AppCompatActivity() {

    private var buttonSelected = -1

    //Initialize today's date
    private val today = Calendar.getInstance().toLocalDate()

    // Initialize the user info.
    var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_test)

        val affiliations = resources.getStringArray(R.array.affiliations_string)
        val affiliationButtons = arrayOf(
            buttonArmy,
            buttonNavy,
            buttonAir,
            buttonMarine,
            buttonPolice,
            buttonSeapolice,
            buttonPublic,
            buttonFire
        )
        val dateInputs = arrayOf(enlistDate, privateDate, corporalDate, sergeantDate, endDate)

        loadData(affiliations, affiliationButtons, dateInputs)
        setAffiliation(affiliations, affiliationButtons)
        setDates(dateInputs)
        completeRegister()
        reset()
    }

    // Load the user info from SharedPreferences.
    private fun loadData(affiliations: Array<String>, buttons: Array<Button>, dateInputs: Array<Button>) {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.contains("userInfo")) {
            userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
            // Display the stored affiliation.
            buttonSelected = affiliations.indexOf(userInfo.affiliation)
            buttons[buttonSelected].setBackgroundResource(R.drawable.gray_gradient)
            // Display the promotion dates.
            for (index in Dates.RANK2.ordinal until dateInputs.size) {
                dateInputs[index].text = formatDate(userInfo.promotionDates[index])
            }
        }
        // Display the stored name.
        nameInput.setText(userInfo.name)
        // Display the enlist date.
        enlistDate.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
    }

    // Save the affiliation for each selection.
    @SuppressLint("PrivateResource")
    private fun setAffiliation(affiliations: Array<String>, buttons: Array<Button>) {
        for ((index, value) in buttons.withIndex()) {
            value.setOnClickListener {
                userInfo.affiliation = affiliations[index]

                // Highlight the selected button.
                if (0 <= buttonSelected && buttonSelected < buttons.size) {
                    buttons[buttonSelected].setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
                }
                buttons[index].setBackgroundResource(R.drawable.gray_gradient)
                buttonSelected = index

                // Update promotion dates corresponding to the affiliation.
                calcPromotionDates()
                updatePromotionViews()
                // Update rank names corresponding to the affiliation.
                ranksByAffiliation()
            }
        }
    }

    private fun setDates(dateInputs: Array<Button>) {
        for ((index, value) in dateInputs.withIndex()) {
            value.setOnClickListener {
                // Alert if the user adjusts promotion dates before selecting an affiliation.
                if (index != Dates.ENLIST.ordinal && buttonSelected == -1) {
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
        register.setOnClickListener {
            when {
                isEmpty(nameInput.text.toString()) -> {
                    // https://github.com/pranavpandey/dynamic-toasts
                    DynamicToast.makeError(this, "이름을 입력해주세요!").show()
                    return@setOnClickListener
                }
                buttonSelected == -1 -> {
                    // https://github.com/pranavpandey/dynamic-toasts
                    DynamicToast.makeError(this, "군별을 골라주세요!").show()
                    return@setOnClickListener
                }
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
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        userInfo.name = nameInput.text.toString()
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

    private fun reset() {
        reset.setOnClickListener {
            when {
                isEmpty(nameInput.text.toString()) -> {
                    // https://github.com/pranavpandey/dynamic-toasts
                    DynamicToast.makeError(this, "이름을 입력해주세요!").show()
                    return@setOnClickListener
                }
                buttonSelected == -1 -> {
                    // https://github.com/pranavpandey/dynamic-toasts
                    DynamicToast.makeError(this, "군별을 골라주세요!").show()
                    return@setOnClickListener
                }
                else -> {
                    DynamicToast.makeError(this, "조정 사항이 입대일을 기준으로 초기화됩니다!").show()
                    calcPromotionDates()
                    updatePromotionViews()
                }
            }
        }
    }

    private fun updatePromotionViews() {
        enlistDate.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
        privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
        endDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
    }
}
