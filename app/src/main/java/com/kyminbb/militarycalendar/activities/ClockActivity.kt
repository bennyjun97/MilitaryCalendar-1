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
        progressBar.progress = DateCalc.entirePercent(enlistDateTime, etsDateTime).toInt()
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}