package com.kyminbb.militarycalendar.activities.main

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        calendar.add(Calendar.MONTH, 12)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        var year = calendar.get(Calendar.YEAR)
        textMonth.text = "${month}${day}${year}월"

        buttonAdd.setOnClickListener {
            if (adding) {
                addTab.visibility = View.VISIBLE
            } else {
                addTab.visibility = View.GONE
            }
            adding = !adding
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
}
