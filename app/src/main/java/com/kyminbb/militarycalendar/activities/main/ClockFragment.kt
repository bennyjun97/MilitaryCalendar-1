package com.kyminbb.militarycalendar.activities.main


import android.content.res.Resources
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
import kotlinx.android.synthetic.main.fragment_clock.*
import org.threeten.bp.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class ClockFragment : Fragment() {

    private var userInfo = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadData()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clock, container, false)
    }

    // Update UI after views are created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val enlistDateTime = LocalDateTime.of(
            userInfo.promotionDates[Dates.ENLIST.ordinal].year,
            userInfo.promotionDates[Dates.ENLIST.ordinal].month,
            userInfo.promotionDates[Dates.ENLIST.ordinal].dayOfMonth,
            0, 0, 0, 0
        )
        val etsDateTime = LocalDateTime.of(
            userInfo.promotionDates[Dates.END.ordinal].year,
            userInfo.promotionDates[Dates.END.ordinal].month,
            userInfo.promotionDates[Dates.END.ordinal].dayOfMonth,
            0, 0, 0, 0
        )

        progressBar.max = 100
        val percent = DateCalc.entirePercent(enlistDateTime, etsDateTime)
        progressBar.progress = percent.toInt()

        val hour = percent * 0.24
        val min = (percent * 14.4) % 60
        digitalTime.text = formatTime(hour.toInt(), min.toInt())
        clockView.onTimeChanged(hour.toLong(), min.toLong())
    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    private fun formatTime(hour: Int, min: Int) : String {
        return "%02d".format(hour) + " : " + "%02d".format(min)
    }
}
