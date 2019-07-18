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

        //cloning just in case
        val cal = calendar.clone() as Calendar

        //putting numbers for month
        var month = cal.get(Calendar.MONTH)
        textMonth.text = "${month+1}월"
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val init = cal.get(Calendar.DAY_OF_WEEK)-1
        var position = init

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

        val leaveText1 = TextView(this.context)
        val constraintSet = ConstraintSet()
        leaveText1.id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(leaveText1.id, ConstraintSet.START, day30.id, ConstraintSet.START)
        constraintSet.connect(leaveText1.id, ConstraintSet.END, day32.id, ConstraintSet.END)
        constraintSet.connect(leaveText1.id, ConstraintSet.BOTTOM, day30.id, ConstraintSet.BOTTOM)
        constraintSet.connect(leaveText1.id, ConstraintSet.TOP, day30.id, ConstraintSet.TOP)
        constraintSet.constrainWidth(leaveText1.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(leaveText1.id, ConstraintSet.WRAP_CONTENT)
        leaveText1.setBackgroundColor(R.drawable.textleavebackground)
        leaveText1.text = "와~~집에 간다!"
        leaveText1.textSize = 8.0f
        calendarLayout.addView(leaveText1)
        constraintSet.applyTo(calendarLayout)
    }
}
