package com.kyminbb.militarycalendar.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.activity_clock.*
import org.threeten.bp.LocalDateTime

class ClockActivity : AppCompatActivity() {
    private var userInfo = User()

    // 복무율에 따라 강을 설정해주고, 돗단배가 강을 흘러 간다. 이때, 선착장이 있는데, 일병, 이병, 상병, 병장이 될때마다 선착하고, 깃발의 색깔이 바뀐다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        loadData()
        var enlistDateTime = LocalDateTime.of(userInfo.promotionDates[Dates.ENLIST.ordinal].year,
            userInfo.promotionDates[Dates.ENLIST.ordinal].month,
            userInfo.promotionDates[Dates.ENLIST.ordinal].dayOfMonth,
            0, 0, 0, 0)
        var etsDateTime = LocalDateTime.of(userInfo.promotionDates[Dates.END.ordinal].year,
            userInfo.promotionDates[Dates.END.ordinal].month,
            userInfo.promotionDates[Dates.END.ordinal].dayOfMonth,
            0, 0, 0, 0)

        progressBar.max = 100
        var percent = DateCalc.entirePercent(enlistDateTime, etsDateTime)
        progressBar.progress = percent.toInt()

        var hour = percent*0.24
        var min = (percent*14.4)%60

        digitalTime.text = "${hour.toInt()}:${min.toInt()}"

        clockView.onTimeChanged(hour.toLong(), min.toLong())
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}