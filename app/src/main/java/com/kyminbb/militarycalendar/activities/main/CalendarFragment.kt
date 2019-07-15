package com.kyminbb.militarycalendar.activities.main

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.database.DBHelper
import com.kyminbb.militarycalendar.database.TableReaderContract
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.jetbrains.anko.support.v4.toast
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
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    // Update UI after views are created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = DBHelper(this.context!!)

        slots = arrayOf(slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10, slot11, slot12, slot13, slot14, slot15, slot16, slot17, slot18, slot19, slot20, slot21, slot22, slot23, slot24, slot25, slot26, slot27, slot28, slot29, slot30, slot31, slot32, slot33, slot34, slot35, slot36, slot37, slot38, slot39, slot40, slot41, slot42)

        updateCalendar(calendar)

        buttonAdd.setOnClickListener {
            if (adding) {
                addTab.visibility = View.VISIBLE
            } else {
                addTab.visibility = View.GONE
            }
            adding = !adding
        }

        buttonRight.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar(calendar)
        }

        buttonLeft.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar(calendar)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
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
        }
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
        for(i in 0..41) {
            slots[i].text = ""
        }
        val cal = calendar.clone() as Calendar
        var month = cal.get(Calendar.MONTH)
        textMonth.text = "${month+1}월"
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val init = cal.get(Calendar.DAY_OF_WEEK)-1
        var position = init
        val cal2 = cal.clone() as Calendar
        cal2.add(Calendar.MONTH, 1)
        cal2.add(Calendar.DAY_OF_MONTH, -1)
        val j = cal2.get(Calendar.DAY_OF_MONTH) - 1
        for(i in 0..j) {
            slots[position].text = cal.get(Calendar.DAY_OF_MONTH).toString()
            position += 1
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
