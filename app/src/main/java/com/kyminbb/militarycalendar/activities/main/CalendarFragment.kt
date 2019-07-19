package com.kyminbb.militarycalendar.activities.main

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.database.DBHelper
import com.kyminbb.militarycalendar.database.TableReaderContract
import kotlinx.android.synthetic.main.fragment_calendar2.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import java.util.*


class CalendarFragment : Fragment() {

    private var adding = true
    var calendar = Calendar.getInstance()
    var slots: Array<Button> = arrayOf()
    var startSlot = 0
    var leaveTextViewNum = 0
    var leavesinMonth: MutableList<TextView> = mutableListOf<TextView>()  //stores all the leave of the month. updated by updateCalendar()
    /*var dutiesinMonth: MutableList<Duty> = mutableListOf<Duty>() //stores all the duty of the month. updated by updateCalendar()
    var exercisesinMonth: MutableList<Exercise> = mutableListOf<Exerciese>() //stores all the exercise of the month. updated by updateCalendar()
    var personalinMonth: MutableList<Personal> = mutableListOf<Personal>()
    */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar2, container, false)
    }

    // Update UI after views are created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = DBHelper(this.context!!)

        slots = arrayOf(day1, day2, day3, day4, day5, day6, day7, day8, day9, day10, day11, day12, day13, day14, day15, day16, day17, day18, day19, day20, day21, day22, day23, day24, day25, day26, day27, day28, day29, day30, day31, day32, day33, day34, day35, day36, day37, day38, day39, day40, day41, day42)

        updateCalendar(calendar)


        addLeave.setOnClickListener {
            if(!adding)
                return@setOnClickListener
            else {

            }
        }

        buttonAdd.setOnClickListener {
            if (adding) {
                addTab.visibility = View.VISIBLE
            } else {
                addTab.visibility = View.GONE
            }
            adding = !adding
        }

        buttonRight.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, 1)
            updateCalendar(calendar)
        }

        buttonLeft.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -1)
            updateCalendar(calendar)
        }

        /*(calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = formatDate(year, month+1, dayOfMonth)

            // Show saved schedules of the date.
            val contents = readDB(dbHelper, date)
            toast("$contents")

            // Add a schedule on the date.
            val addButtons = mapOf(addLeave to "휴가", addDuty to "당직", addExercise to "훈련", addPersonal to "개인일정")
            for (button in addButtons.keys) {
                button.setOnClickListener {
                    writeDB(dbHelper, (addButtons[button])!!, date)
                    val text = "${addButtons[button]} 추가"
                    testText.text = text
                }
            }
        }*/
    }

    private fun writeDB(dbHelper: DBHelper, content: String, date: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TableReaderContract.TableEntry.COLUMN_START_DATE, date)
            put(TableReaderContract.TableEntry.COLUMN_END_DATE, date)
            put(TableReaderContract.TableEntry.COLUMN_CONTENT, content)
        }
        db?.insert(TableReaderContract.TableEntry.TABLE_NAME, null, values)
    }

    private fun readDB(dbHelper: DBHelper, date: String): MutableList<String> {
        val db = dbHelper.readableDatabase

        // Define a projection that specifies which columns from the database you will actually find after the query.
        val projection = arrayOf(
            BaseColumns._ID,
            TableReaderContract.TableEntry.COLUMN_START_DATE,
            TableReaderContract.TableEntry.COLUMN_END_DATE,
            TableReaderContract.TableEntry.COLUMN_CONTENT
        )

        val selection = "${TableReaderContract.TableEntry.COLUMN_START_DATE} = ?"
        val selectionArgs = arrayOf(date)

        val sortOrder = "${TableReaderContract.TableEntry.COLUMN_END_DATE} ASC"

        val cursor = db.query(
            TableReaderContract.TableEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )

        val contents = mutableListOf<String>()
        // Read the column's value by iterating through results.
        with(cursor) {
            while (moveToNext()) {
                val content = getString(getColumnIndexOrThrow(TableReaderContract.TableEntry.COLUMN_CONTENT))
                if (content != null) {
                    contents.add(content)
                }
            }
        }
        cursor.close()
        return contents
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        return "$year-$month-$dayOfMonth"
    }

    private fun updateCalendar(calendar: Calendar) {
        //clearing slots
        for(i in 0..41) {
            slots[i].text = ""
            slots[i].setBackgroundResource(0)
        }

        //clearing textViews
        if(leaveTextViewNum != 0) {
            calendarLayout.removeView(leavesinMonth[0])
            if(leaveTextViewNum >=2)
                calendarLayout.removeView(leavesinMonth[1])
            for (leave in leavesinMonth) {
                calendarLayout.removeView(leave)
            }
            leavesinMonth.clear()
        }
        leaveTextViewNum = 0

        //cloning just in case
        val cal = calendar.clone() as Calendar

        //putting numbers for month
        var month = cal.get(Calendar.MONTH)
        textMonth.text = "${month+1}월"
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val init = cal.get(Calendar.DAY_OF_WEEK)-1
        var position = init
        startSlot = init

        //calculating last day
        val cal2 = cal.clone() as Calendar
        cal2.add(Calendar.MONTH, 1)
        cal2.add(Calendar.DAY_OF_MONTH, -1)
        val j = cal2.get(Calendar.DAY_OF_MONTH) - 1

        //putting numbers for days
        for(i in 0..j) {
            slots[position].text = cal.get(Calendar.DAY_OF_MONTH).toString()
            position += 1
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // circle on today
        // https://stackoverflow.com/questions/25203501/android-creating-a-circular-textview
        val today = Calendar.getInstance()
        if (cal2.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal2.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            val todayposition = today.get(Calendar.DAY_OF_MONTH)
            slots[todayposition].setBackgroundResource(R.drawable.rounded_textview)
        }

        val cal3 = cal2.clone() as Calendar
        cal2.set(Calendar.DAY_OF_MONTH, 15)
        cal3.set(Calendar.DAY_OF_MONTH, 19)
        addLeaveinCalendar(cal2, cal3, "영덩이 찰싹")

    }

    fun addLeaveinCalendar(startDate: Calendar, endDate: Calendar, text: String) {
        var startPosition = startDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1
        var endPosition = endDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        if((startPosition) / 7 == (endPosition) / 7)
            drawLeaveTextView(startPosition, endPosition, text)

        else {
            drawLeaveTextView(startPosition, (startPosition/7) * 7 + 6, text)
            for (i in (startPosition / 7)+1..(endPosition / 7) - 1)
                drawLeaveTextView(i * 7, i * 7 + 6, text)
            drawLeaveTextView((endPosition / 7) * 7, endPosition, text)
        }

    }

    //drawing Leave Text Views on fragment_calendar2 programmatically
    private fun drawLeaveTextView(startPos : Int, endPos : Int, text: String) {
        val constraintSet = ConstraintSet()
        leavesinMonth.add(TextView(this.context))
        leavesinMonth[leaveTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(leavesinMonth[leaveTextViewNum].id, ConstraintSet.START, slots[startPos].id, ConstraintSet.START)
        constraintSet.connect(leavesinMonth[leaveTextViewNum].id, ConstraintSet.END, slots[endPos].id, ConstraintSet.END)
        constraintSet.connect(leavesinMonth[leaveTextViewNum].id, ConstraintSet.BOTTOM, slots[startPos].id, ConstraintSet.BOTTOM)
        constraintSet.connect(leavesinMonth[leaveTextViewNum].id, ConstraintSet.TOP, slots[endPos].id, ConstraintSet.TOP)
        constraintSet.constrainWidth(leavesinMonth[leaveTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(leavesinMonth[leaveTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        leavesinMonth[leaveTextViewNum].setBackgroundColor(Color.parseColor("#7CB342"))
        leavesinMonth[leaveTextViewNum].text = text
        leavesinMonth[leaveTextViewNum].textSize = 8.0f
        calendarLayout.addView(leavesinMonth[leaveTextViewNum])
        constraintSet.applyTo(calendarLayout)
        leaveTextViewNum++
    }
}
