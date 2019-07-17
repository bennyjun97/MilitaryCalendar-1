package com.kyminbb.militarycalendar.activities.main


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.fragment_clock2.*


class ClockFragment : Fragment() {

    private var userInfo = User()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clock2, container, false)
    }

    // Update UI after views are created.
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // load LocalDate data
        val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()


        /**
         * set ProgressBars:
         * progressTotal, progressRank, progressHobong
         */
        progressBarTotal.max = 100
        val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
        progressBarTotal.progress = percentTotal.toInt()

        progressBarRank.max = 100
        val percentRank = DateCalc.rankPercent(userInfo)
        progressBarRank.progress = percentRank.toInt()

        progressBarHobong.max = 100
        val percentHobong = DateCalc.monthPercent(userInfo)
        progressBarHobong.progress = percentHobong.toInt()

        /**
         * set Clock Design
         */
        val hour = percentTotal * 0.24
        val min = (percentTotal * 14.4) % 60
        val sec = (percentTotal * 864.0) % 60
        clockText.text = formatTime(hour.toInt(), min.toInt(), sec.toInt())
        clockView.onTimeChanged(hour.toLong(), min.toLong())


        /**
         * setTextView data
         */
        // set Rank, Hobong
        val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
        rankText.text = rankString
        progressRankText.text = rankString
        progressMonthRankText.text = rankString
        progressMonthText.text = "${DateCalc.calcMonth(userInfo)}호봉"

        // set Text in the Top View
        nameText.text = userInfo.name
        remainText.text = "전역까지 ${DateCalc.countDDay(etsDateTime)}"

        // set Percent Texts in the Bottom View
        entirePercentText.text = "%.6f".format(percentTotal) + "%"
        rankPercentText.text = "${"%.2f".format(percentRank)}%"
        progressMonthPercentText.text = "${"%.2f".format(percentHobong)}%"
    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int, sec: Int): String {
        return "%02d".format(hour) + ":" + "%02d".format(min) + ":" + "%02d".format(sec)
    }
}
