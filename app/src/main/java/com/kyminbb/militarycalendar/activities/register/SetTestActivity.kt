package com.kyminbb.militarycalendar.activities.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlinx.android.synthetic.main.activity_set_test.*
import org.jetbrains.anko.startActivity
import java.util.*

class SetTestActivity : AppCompatActivity() {

    //Initialize today's date
    private val today = Calendar.getInstance().toLocalDate()
    private val todayYear = today.year
    private val todayMonth = today.monthValue
    private val todayDay = today.dayOfMonth

    private val affiliations = arrayOf("육군", "해군", "공군", "해병대", "의경", "해양의무경찰", "사회복무요원", "의무소방대", "해양의무경찰")

    // Initialize the user info.
    var userInfo = User()

    var affilChosen = false
    var buttonNum = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_set_test)

        loadData()

        val buttons = arrayOf(
            buttonArmy,
            buttonNavy,
            buttonAir,
            buttonMarine,
            buttonPolice,
            buttonSeapolice,
            buttonPublic,
            buttonFire
        )

        //If userInfo empty, fill the enlist date with today's date
        if (userInfo.promotionDates.isEmpty()) {
            userInfo.promotionDates[Dates.ENLIST.ordinal] = today
            startDate.text = formatDate(today)
        }
        //If userInfo has data, fill the enlist, promotion, ets dates with loaded data
        else {
            userInfo.promotionDates[Dates.ENLIST.ordinal] = today // 나중에 지우기
            startDate.text = formatDate(today) // 나중에 지우기
            /*startDate.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
            privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
            corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
            sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
            endDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])  나중에 풀기 */
        }

        // Save the affiliation for each selection.
        for ((index, value) in buttons.withIndex()) {
            value.setOnClickListener {
                userInfo.affiliation = affiliations[index]

                if(buttonNum != 10)
                    buttons.get(buttonNum).setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

                buttons.get(index).setBackgroundResource(R.drawable.gray_gradient)
                buttonNum = index

                datesByAffil(userInfo.promotionDates[Dates.ENLIST.ordinal], affiliations[index])
                ranksByAffil(affiliations[index])

                affilChosen = true
            }
        }

        startDate.setOnClickListener {
            setDate(0)
        }

        privateDate.setOnClickListener {
            if(!affilChosen) {
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            }
            setDate(1)
        }

        corporalDate.setOnClickListener {
            if(!affilChosen) {
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            }
            setDate(2)
        }

        sergeantDate.setOnClickListener {
            if(!affilChosen) {
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            }
            setDate(3)
        }

        endDate.setOnClickListener {
            if(!affilChosen) {
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            }
            setDate(4)
        }

        //이름 입력
        set.setOnClickListener {
            if(TextUtils.isEmpty(nameInput.text.toString())) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "이름을 입력해주세요!").show()
                return@setOnClickListener
            }
            else if(!affilChosen) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            }
            else {
                saveData()
                startActivity<HomeActivity>()
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }

        reset.setOnClickListener {
            if (TextUtils.isEmpty(nameInput.text.toString())) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "이름을 입력해주세요!").show()
                return@setOnClickListener
            } else if (!affilChosen) {
                // https://github.com/pranavpandey/dynamic-toasts
                DynamicToast.makeError(this, "군별을 골라주세요!").show()
                return@setOnClickListener
            } else {
                DynamicToast.makeError(this, "조정 사항이 입대일을 기준으로 초기화됩니다!").show()

                datesByAffil(userInfo.promotionDates[Dates.ENLIST.ordinal], userInfo.affiliation)
            }
        }
    }

    // Load the user info from SharedPreferences.
    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.contains("userInfo")) {
            userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
        }
    }

    private fun setDate(dateIndex : Int) {
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            //startDate 눌렀을 때
            if(dateIndex == 0) {
                userInfo.promotionDates[Dates.ENLIST.ordinal] = LocalDate.of(year, month + 1, day)
                startDate.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
                calcEndDate()
                calcPromotionDates()
            }
            // 전역일 조정 시
            else if(dateIndex == 4) {
                userInfo.promotionDates[Dates.END.ordinal] = LocalDate.of(year, month + 1, day)
                endDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
            }

            //진급일 조정 시
            else {
                if(dateIndex == 1) {
                    userInfo.promotionDates[Dates.RANK2.ordinal] = LocalDate.of(year, month + 1, day)
                    privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
                }
                else if(dateIndex == 2) {
                    userInfo.promotionDates[Dates.RANK3.ordinal] = LocalDate.of(year, month + 1, day)
                    corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
                }
                else {
                    userInfo.promotionDates[Dates.RANK4.ordinal] = LocalDate.of(year, month + 1, day)
                    sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
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


        if (userInfo.promotionDates.isEmpty()) {
            dialog.defaultDate(todayYear, todayMonth, todayDay)
        } else {
            dialog.defaultDate(
                userInfo.promotionDates[dateIndex].year,
                userInfo.promotionDates[dateIndex].monthValue - 1,
                userInfo.promotionDates[dateIndex].dayOfMonth
            )
        }
        dialog.build().show()
    }

    private fun calcEndDate() {
        //call calcETS method of dateCalc to calculate ETS date
        userInfo.promotionDates[Dates.END.ordinal] =
            DateCalc.calcETS(userInfo.promotionDates[Dates.ENLIST.ordinal], userInfo.affiliation)
        endDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
    }

    //일병, 상병, 병장 진급일 계산
    //현재 계급 값 설정 및 다음 진급일 표시 기능
    private fun calcPromotionDates() {
        val enlist = userInfo.promotionDates[Dates.ENLIST.ordinal]
        //각각 일병, 상병, 병장 진급일 계산
        userInfo.promotionDates[Dates.RANK2.ordinal] = DateCalc.calcRank2(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK3.ordinal] = DateCalc.calcRank3(enlist, userInfo.affiliation)
        userInfo.promotionDates[Dates.RANK4.ordinal] = DateCalc.calcRank4(enlist, userInfo.affiliation)

        privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])

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

    // Save the user info to SharedPreferences.
    private fun saveData() {
        userInfo.name = nameInput.text.toString()
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        // save the data into UserData Class
        val jsonString = Gson().toJson(userInfo)
        prefs.edit().putString("userInfo", jsonString).putBoolean("firstStart", false).apply()
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
    }

    private fun datesByAffil(enlistDate: LocalDate, affiliation: String) {
        userInfo.promotionDates[Dates.RANK2.ordinal] = DateCalc.calcRank2(enlistDate, affiliation)
        privateDate.text = formatDate(userInfo.promotionDates[Dates.RANK2.ordinal])
        userInfo.promotionDates[Dates.RANK3.ordinal] = DateCalc.calcRank3(enlistDate, affiliation)
        corporalDate.text = formatDate(userInfo.promotionDates[Dates.RANK3.ordinal])
        userInfo.promotionDates[Dates.RANK4.ordinal] = DateCalc.calcRank4(enlistDate, affiliation)
        sergeantDate.text = formatDate(userInfo.promotionDates[Dates.RANK4.ordinal])
        userInfo.promotionDates[Dates.END.ordinal] = DateCalc.calcETS(enlistDate, affiliation)
        endDate.text = formatDate(userInfo.promotionDates[Dates.END.ordinal])
    }

    private fun ranksByAffil(affiliation: String) {
        privateText.text = DateCalc.rankString(1, affiliation) + " 진급일"
        corporalText.text = DateCalc.rankString(2, affiliation) + " 진급일"
        sergeantText.text = DateCalc.rankString(3, affiliation) + " 진급일"
    }

    /*override fun onBackPressed() {
        finish()
    }*/
}
