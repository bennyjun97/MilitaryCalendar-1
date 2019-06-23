package com.kyminbb.militarycalendar.activities.register

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.commit451.addendum.threetenabp.toLocalDate
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.GraphActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.DateUtils.formatDate
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.activity_set_aff.*
import kotlinx.android.synthetic.main.activity_set_enlist.*
import kotlinx.android.synthetic.main.activity_set_name.*
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class SetEnlistActivity : AppCompatActivity() {

    private var userInfo = User()

    private val today = Calendar.getInstance().toLocalDate()
    private val todayYear = today.year
    private val todayMonth = today.monthValue
    private val todayDay = today.dayOfMonth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_enlist)

        loadData()

        // Show ToastMessage
        DynamicToast.makeSuccess(this, "${userInfo.affiliation}이시군요!\n언제 입대하셨나요?").show()
        //toast("${userInfo.affiliation} 이시군요!\n언제 입대하셨나요?")

        //set Date with spinnerDate
        inputEnlist.setOnClickListener {
            setDate()
        }


        // Action when next button is called
        nextEnlistButton.setOnClickListener {
            if (TextUtils.isEmpty(inputEnlist.text.toString())) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "입대일을 입력해주세요!").show()
                return@setOnClickListener
            } else {
                saveData()
                // Transition to the next activity.
                startActivity<GraphActivity>()
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }

        // Go back to SetAffActivity when back button is called
        backEnlistButton.setOnClickListener {
            startActivity<SetAffActivity>()
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

    private fun saveData() {
        userInfo.promotionDates[Dates.ENLIST.ordinal] = LocalDate.parse(inputEnlist.text.toString())
        val jsonString = Gson().toJson(userInfo)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        prefs.edit().putString("userInfo", jsonString).putBoolean("firstStart", false).apply()
    }

    private fun setDate() {
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            userInfo.promotionDates[Dates.ENLIST.ordinal] = LocalDate.of(year, month + 1, day)
            inputEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
            calcEndDate()
            calcPromotionDates()
        }

        val dialog = SpinnerDatePickerDialogBuilder()
            .context(this)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(todayYear + 4, 11, 31)
            .minDate(todayYear - 5, 0, 1)


        if (userInfo.promotionDates.isEmpty()) {
            dialog.defaultDate(todayYear, todayMonth, todayDay)
        } else {
            dialog.defaultDate(
                userInfo.promotionDates[Dates.ENLIST.ordinal].year,
                userInfo.promotionDates[Dates.ENLIST.ordinal].monthValue - 1,
                userInfo.promotionDates[Dates.ENLIST.ordinal].dayOfMonth
            )
        }
        dialog.build().show()
    }

    private fun calcEndDate() {
        //call calcETS method of dateCalc to calculate ETS date
        userInfo.promotionDates[Dates.END.ordinal] =
            DateCalc.calcETS(userInfo.promotionDates[Dates.ENLIST.ordinal], userInfo.affiliation)
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
        if (today.isBefore(userInfo.promotionDates[Dates.RANK2.ordinal])) {
            userInfo.rank = 0
        } else if (today.isBefore(userInfo.promotionDates[Dates.RANK3.ordinal])) {
            userInfo.rank = 1
        } else if (today.isBefore(userInfo.promotionDates[Dates.RANK4.ordinal])) {
            userInfo.rank = 2
        } else {
            userInfo.rank = 3
        }
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
    }
}
