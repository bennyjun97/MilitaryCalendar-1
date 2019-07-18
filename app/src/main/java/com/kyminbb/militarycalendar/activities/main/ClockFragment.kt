package com.kyminbb.militarycalendar.activities.main


import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.register.SetTestActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.fragment_clock2.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.startActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.lang.ref.WeakReference
import java.util.*


class ClockFragment : Fragment() {

    private var userInfo = User()
    private var isRunning : Boolean = false
    //private var handler : DisplayHandler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()
        // 우선 핸들러 객체 생성
        //handler = DisplayHandler()
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
            startActivity<SetTestActivity>()
        }

        // set Text in the Top View
        nameText.text = userInfo.name
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }


    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int, sec: Int): String {
        return "%02d".format(hour) + ":" + "%02d".format(min) + ":" + "%02d".format(sec)
    }

    /*inner class AsyncTaskClass : AsyncTask<LocalDateTime, Double, String> () {

        override fun doInBackground(vararg p0: LocalDateTime?): String {
            for (index in 0 .. 9){
                SystemClock.sleep(1000)
                val percentTotal = DateCalc.entirePercent(p0[0]!!, p0[1]!!)
                publishProgress(percentTotal)
            }
            return ""
        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
            entirePercentText.text = "%.10f".format(values[0]) + "%"
        }
    }*/


    // android 8.0 이하부터는 개발자가 일반쓰레드의 작업에서 뷰를 변형시키위해서는 핸들러를 사용해야한다
    // 핸들러를 상속받는 클래스를 우선 만들자
    inner class ThreadClass: Thread(){
        override fun run() {

            val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
            val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()

            val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)

            while(isRunning){


                // calc percents
                val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
                val percentRank = DateCalc.rankPercent(userInfo)
                val percentHobong = DateCalc.monthPercent(userInfo)

                // clock infos
                val hour = percentTotal * 0.24
                val min = (percentTotal * 14.4) % 60
                val sec = (percentTotal * 864.0) % 60

                // message 객체를 만들고 값을 넣는다.
                runOnUiThread {
                    // update percent text
                    entirePercentText.text = "%.10f".format(percentTotal) + "%"
                    rankPercentText.text = "${"%.4f".format(percentRank)}%"
                    progressMonthPercentText.text = "${"%.4f".format(percentHobong)}%"

                    // update progressbars
                    progressBarTotal.progress = percentTotal.toInt()
                    progressBarRank.progress = percentRank.toInt()
                    progressBarHobong.progress = percentHobong.toInt()

                    // update clock infos
                    clockText.text = formatTime(hour.toInt(), min.toInt(), sec.toInt())
                    clockView.onTimeChanged(hour.toLong(), min.toLong())


                    // set Rank, Hobong related strings
                    rankText.text = rankString
                    progressRankText.text = rankString
                    progressMonthRankText.text = rankString
                    progressMonthText.text = "${DateCalc.calcMonth(userInfo)}호봉"

                    // update D-day
                    remainText.text = "전역까지 ${DateCalc.countDDay(etsDateTime)}"
                }

            }

        }
    }
    /*
    inner class ThreadClass: Thread(){
        override fun run() {

            val enlistDateTime = userInfo.promotionDates[Dates.ENLIST.ordinal].atStartOfDay()
            val etsDateTime = userInfo.promotionDates[Dates.END.ordinal].plusDays(1).atStartOfDay()

            val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)


            while(isRunning) {


                // calc percents
                val percentTotal = DateCalc.entirePercent(enlistDateTime, etsDateTime)
                val percentRank = DateCalc.rankPercent(userInfo)
                val percentHobong = DateCalc.monthPercent(userInfo)

                // clock infos
                val hour = percentTotal * 0.24
                val min = (percentTotal * 14.4) % 60
                val sec = (percentTotal * 864.0) % 60


                // construct messages

                val msgPercent = Message()
                msgPercent.what = 0
                msgPercent.obj =
                    arrayOf(
                        "%.10f".format(percentTotal) + "%",
                        "${"%.4f".format(percentRank)}%",
                        "${"%.4f".format(percentHobong)}%")


                val msgProgressBar = Message()
                msgProgressBar.what = 1
                msgProgressBar.obj =
                    arrayOf(
                    percentTotal.toInt(),
                    percentRank.toInt(),
                    percentHobong.toInt())

                val msgClock = Message()
                msgClock.what = 2
                msgClock.obj = arrayOf(hour, min, sec)

                val msgRankString = Message()
                msgRankString.what = 3
                msgRankString.obj =
                    arrayOf(
                        rankString,
                        "${DateCalc.calcMonth(userInfo)}호봉",
                        "전역까지 ${DateCalc.countDDay(etsDateTime)}")


                handler?.sendMessage(msgPercent)
                handler?.sendMessage(msgProgressBar)
                handler?.sendMessage(msgClock)
                handler?.sendMessage(msgRankString)

            }

        }
    }

    class DisplayHandler internal constructor(context: ClockFragment): Handler() {

        private val activityReference: WeakReference<ClockFragment> = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            // 보통 여기에서는 화면에 보이는 작업을 많이하고, 데이터를 구하는 작업은 하지않는다
            // 데이터를 받아와서 값을 보여주는 작업위주로 한다.
            // 아래코드 같은 경우에는 현재 시간의 값을 구한다. 이것은 사실 비추
            // 그렇다면, 이러한 값들을 전달 받기 위해서는 어떻게 해야할까? 바로 Thread 클래스에서 Message객체를 생성한다.
            /*val time = System.currentTimeMillis()
            textView4.text = "Handler ${time}"*/
            val activity = activityReference.get()
            if (activity == null) return
            if (msg.what == 0) {
                val entirePercentText =
                entirePercentText.text = "%.10f".format(percentTotal) + "%"
                rankPercentText.text = "${"%.4f".format(percentRank)}%"
                progressMonthPercentText.text = "${"%.4f".format(percentHobong)}%"
            }
        }
    }*/

}
