package com.kyminbb.militarycalendar.activities.main


import android.annotation.SuppressLint
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.register.RegisterActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.fragment_clock2.*
import kotlinx.coroutines.*
import org.jetbrains.anko.support.v4.startActivity
import org.threeten.bp.LocalDateTime


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

        buttonEdit.setOnClickListener {
            startActivity<RegisterActivity>()
        }

        // set Text in the Top View
        nameText.text = userInfo.name

        val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()

        // update datas concurrently until main thread is finished
        GlobalScope.launch(Dispatchers.Main){
            while(true) {
                delay(100)
                upDateInfos(enlistDateTime, etsDateTime)
            }
        }

    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int, sec: Int): String {
        return "%02d".format(hour) + ":" + "%02d".format(min) + ":" + "%02d".format(sec)
    }

    private fun upDateInfos(enlistDateTime: LocalDateTime, etsDateTime: LocalDateTime){

        // calc percents
        val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
        val percentRank = DateCalc.rankPercent(userInfo)
        val percentHobong = DateCalc.monthPercent(userInfo)

        // update percent text
        entirePercentText.text = "%.8f".format(percentTotal) + "%"
        rankPercentText.text = "${"%.2f".format(percentRank)}%"
        progressMonthPercentText.text = "${"%.2f".format(percentHobong)}%"

        // update progressbars
        progressBarTotal.progress = percentTotal.toInt()
        progressBarRank.progress = percentRank.toInt()
        progressBarHobong.progress = percentHobong.toInt()

        // update clock infos
        val hour = percentTotal * 0.24
        val min = (percentTotal * 14.4) % 60
        val sec = (percentTotal * 864.0) % 60
        clockText.text = formatTime(hour.toInt(), min.toInt(), sec.toInt())
        clockView.onTimeChanged(hour.toLong(), min.toLong())


        // set Rank, Hobong related strings
        val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
        rankText.text = rankString
        progressRankText.text = rankString
        progressMonthRankText.text = rankString
        progressMonthText.text = "${DateCalc.calcMonth(userInfo)}호봉"


        // update D-day
        remainText.text = "전역까지 ${DateCalc.countDDay(etsDateTime)}"
    }
}