package com.kyminbb.militarycalendar.activities.main


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.SettingActivity
import com.kyminbb.militarycalendar.utils.ClockView
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.fragment_clock2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.startActivity


class ClockFragment: Fragment() {

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

        // set Text in the Top View
        nameText.text = userInfo.name

        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].atStartOfDay()
        untilEndText.text = "전역까지 ${DateCalc.countDDay(etsDateTime)}"

        buttonEdit.setOnClickListener {
            startActivity<SettingActivity>()
        }

        // update percentages using co-routine
        updateTotal(entirePercentText, progressBarTotal)
        updateRank(rankPercentText, progressBarRank)
        updateHobong(progressMonthPercentText, progressBarHobong)
        updateClock(clockText, clockView)
        updateRankText(rankText, progressRankText, progressMonthRankText, progressMonthText)
    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int, sec: Int): String {
        return "%02d".format(hour) + ":" + "%02d".format(min) + ":" + "%02d".format(sec)
    }

    /** Co-routine functions**/
    private fun updateTotal(percentText: TextView, progressBar: ProgressBar) {
        val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].atStartOfDay()

        val job = GlobalScope.launch(Dispatchers.Main) { // launch coroutine in the main thread
            while(true) {
                val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
                percentText.text = "${"%.8f".format(percentTotal)}%"
                progressBar.progress = percentTotal.toInt()
                delay(10)
            }
        }
        if(!HomeActivity.isFirstPage){ job.cancel() }
    }

    private fun updateRank(percentText: TextView, progressBar: ProgressBar) {
        val job = GlobalScope.launch(Dispatchers.Main) { // launch coroutine in the main thread
            while(true) {
                val percentTotal = DateCalc.rankPercent(userInfo)
                percentText.text = "${"%.4f".format(percentTotal)}%"
                progressBar.progress = percentTotal.toInt()
                delay(10)
            }
        }
        if(!HomeActivity.isFirstPage){ job.cancel() }
    }

    private fun updateHobong(percentText: TextView, progressBar: ProgressBar) {
        val job = GlobalScope.launch(Dispatchers.Main) { // launch coroutine in the main thread
            while(true) {
                val percentTotal = DateCalc.monthPercent(userInfo)
                percentText.text = "${"%.4f".format(percentTotal)}%"
                progressBar.progress = percentTotal.toInt()
                delay(10)
            }
        }
        if(!HomeActivity.isFirstPage){ job.cancel() }
    }

    // update clock and d-day
    private fun updateClock(textView: TextView, view: ClockView){

        val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
        val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].atStartOfDay()

        val job = GlobalScope.launch(Dispatchers.Main) { // launch coroutine in the main thread
            while(true) {
                val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
                val hour = percentTotal * 0.24
                val min = (percentTotal * 14.4) % 60
                val sec = (percentTotal * 864.0) % 60
                textView.text = formatTime(hour.toInt(), min.toInt(), sec.toInt())
                view.onTimeChanged(hour.toLong(), min.toLong(), sec.toLong())
                //remainText.text = "전역까지 ${DateCalc.countDDay(etsDateTime)}"
                delay(1000)
            }
        }
        if(!HomeActivity.isFirstPage){ job.cancel() }
    }

    private fun updateRankText(rankText: TextView, progressRankText:TextView,
                           progressMonthRankText:TextView, progressMonthText:TextView ){
        val job = GlobalScope.launch (Dispatchers.Main) {
            while(true) {
                val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
                rankText.text = rankString
                progressRankText.text = rankString
                progressMonthRankText.text = rankString
                progressMonthText.text = "${DateCalc.calcMonth(userInfo)}호봉"
                delay(1000)
            }
        }
        if (!HomeActivity.isFirstPage) {job.cancel()}
    }
}