package com.kyminbb.militarycalendar.activities.main


import android.annotation.SuppressLint
import android.os.*
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
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.startActivity


class ClockFragment : Fragment() {

    private var userInfo = User()
    private var isRunning : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()

        // Run subThread
        isRunning = true
        val thread = ThreadClass()
        thread.start()

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


        val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()
        val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)


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

        // set Text in the Top View
        nameText.text = userInfo.name
    }

    // terminate background thread
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int, sec: Int): String {
        return "%02d".format(hour) + ":" + "%02d".format(min) + ":" + "%02d".format(sec)
    }

    // android 8.0 이하부터는 개발자가 일반쓰레드의 작업에서 뷰를 변형시키위해서는 핸들러를 사용해야한다
    // 핸들러를 상속받는 클래스를 우선 만들자
    inner class ThreadClass: Thread(){
        override fun run() {


            val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
            val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()

            while(isRunning){
                SystemClock.sleep(500)
                // calc percents
                val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
                val percentRank = DateCalc.rankPercent(userInfo)
                val percentHobong = DateCalc.monthPercent(userInfo)

                // mainThread 에 요청을 한다.
                runOnUiThread {
                    // update percent text
                    entirePercentText.text = "%.8f".format(percentTotal) + "%"
                    rankPercentText.text = "${"%.2f".format(percentRank)}%"
                    progressMonthPercentText.text = "${"%.2f".format(percentHobong)}%"

                    // update progressbars
                    progressBarTotal.progress = percentTotal.toInt()
                    progressBarRank.progress = percentRank.toInt()
                    progressBarHobong.progress = percentHobong.toInt()
                }
            }

        }
    }

}
