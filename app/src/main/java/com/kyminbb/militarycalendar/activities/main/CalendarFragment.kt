package com.kyminbb.militarycalendar.activities.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.commit451.addendum.threetenabp.toLocalDate
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.database.DBHelper
import com.kyminbb.militarycalendar.database.TableReaderContract
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_calendar2.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*


class CalendarFragment : Fragment() {

    private var adding = true
    private var calendar = Calendar.getInstance()
    private val today = calendar.toLocalDate()

    var slots: Array<Button> = arrayOf()
    var startSlot = 0
    var endSlot = 41
    var leaveTextViewNum = 0
    var dutyTextViewNum = 0
    var exerciseTextViewNum = 0
    var personalTextViewNum = 0
    var leavesinMonth: MutableList<TextView> =
        mutableListOf<TextView>()  //stores all the leave of the month. updated by updateCalendar()
    var dutiesinMonth: MutableList<TextView> =
        mutableListOf<TextView>() //stores all the duty of the month. updated by updateCalendar()
    var exercisesinMonth: MutableList<TextView> =
        mutableListOf<TextView>() //stores all the exercise of the month. updated by updateCalendar()
    var personalinMonth: MutableList<TextView> = mutableListOf<TextView>()

    var daySelected = -1

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

        val dbHelper: DBHelper = DBHelper.getInstance(context!!)

        slots = arrayOf(
            day1,
            day2,
            day3,
            day4,
            day5,
            day6,
            day7,
            day8,
            day9,
            day10,
            day11,
            day12,
            day13,
            day14,
            day15,
            day16,
            day17,
            day18,
            day19,
            day20,
            day21,
            day22,
            day23,
            day24,
            day25,
            day26,
            day27,
            day28,
            day29,
            day30,
            day31,
            day32,
            day33,
            day34,
            day35,
            day36,
            day37,
            day38,
            day39,
            day40,
            day41,
            day42
        )

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
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, 1)
            updateCalendar(calendar)
        }

        buttonLeft.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -1)
            updateCalendar(calendar)
        }

        for (button in slots) {
            button.setOnClickListener {
                if (button.text.isNotEmpty()) {
                    button.setBackgroundResource(R.drawable.calendar_stroke)
                    if (daySelected == -1 || daySelected == Integer.parseInt(button.text.toString()) + startSlot - 1) {
                        daySelected = Integer.parseInt(button.text.toString()) + startSlot - 1
                        return@setOnClickListener
                    } else {
                        slots[daySelected].setBackgroundResource(R.drawable.calendar_button)
                        daySelected = Integer.parseInt(button.text.toString()) + startSlot - 1
                    }
                }
            }
        }
        // 일정 추가 코드!!
        // 밑에 코드 이해 못 해서 일단 이걸로 씀 ㅠㅠ

        val popupView = layoutInflater.inflate(R.layout.add_event, null)
        val popup = PopupWindow(popupView)

        addLeave.setOnClickListener {
            popup.showAtLocation(view, Gravity.CENTER, 0, 0)
            popup.update(
                view,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )

            val startSchedule = popupView.find<Button>(R.id.startSchedule)
            val endSchedule = popupView.find<Button>(R.id.endSchedule)
            val buttonAddEvent = popupView.find<Button>(R.id.buttonAddEvent)
            val buttonCancel = popupView.find<Button>(R.id.buttonCancel)

            startSchedule.setOnClickListener {
                setDate(popupView, "Start")
            }

            endSchedule.setOnClickListener {
                setDate(popupView, "End")
            }

            buttonAddEvent.setOnClickListener {
                if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                    // Store the schedule.
                    writeDB(dbHelper, "휴가", startSchedule.text.toString(), endSchedule.text.toString(), "디비실험")
                    // Retrieve the data for test purpose.
                    val endDates = readDB(dbHelper, startSchedule.text.toString())
                    for (endDate in endDates) {
                        addLeaveinCalendar(
                            string2Date(startSchedule.text.toString()),
                            string2Date(endDate.first),
                            endDate.third
                        )
                    }

                    startSchedule.text = ""
                    endSchedule.text = ""
                    // Dismiss the popup window.
                    popup.dismiss()
                }
            }

            buttonCancel.setOnClickListener {
                // Dismiss the popup window.
                popup.dismiss()
            }
        }
    }

    // To be revised to display the selected date for the default.
    private fun setDate(view: View, type: String) {
        val startSchedule = view.find<Button>(R.id.startSchedule)
        val endSchedule = view.find<Button>(R.id.endSchedule)
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            when (type) {
                "Start" -> startSchedule.text = date2String(year, month + 1, day)
                "End" -> endSchedule.text = date2String(year, month + 1, day)
            }
        }

        val dialog = SpinnerDatePickerDialogBuilder()
            .context(context)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(today.year + 4, 11, 31)
            .minDate(today.year - 5, 0, 1)
            .defaultDate(today.year, today.monthValue - 1, today.dayOfMonth)
        dialog.build().show()
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

    private fun writeDB(dbHelper: DBHelper, content: String, startDate: String, endDate: String, memo: String) {
        dbHelper.use {
            insert(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_START_DATE to startDate,
                TableReaderContract.TableEntry.COLUMN_END_DATE to endDate,
                TableReaderContract.TableEntry.COLUMN_CONTENT to content,
                TableReaderContract.TableEntry.COLUMN_MEMO to memo
            )
        }
    }

    private fun readDB(dbHelper: DBHelper, startDate: String): List<Triple<String, String, String>> {
        return dbHelper.use {
            select(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_END_DATE,
                TableReaderContract.TableEntry.COLUMN_CONTENT,
                TableReaderContract.TableEntry.COLUMN_MEMO
            )
                .whereSimple("${TableReaderContract.TableEntry.COLUMN_START_DATE} = ?", startDate).exec {
                    val parser = rowParser { endDate: String, content: String, memo: String ->
                        Triple(endDate, content, memo)
                    }
                    parseList(parser)
                }
        }
    }

    private fun date2String(year: Int, month: Int, dayOfMonth: Int): String {
        return "$year-$month-$dayOfMonth"
    }

    @SuppressLint("SimpleDateFormat")
    private fun string2Date(date: String): Calendar {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val cal = Calendar.getInstance()
        cal.time = format.parse(date)
        return cal
    }

    private fun updateCalendar(calendar: Calendar) {
        //clearing slots
        for (i in 0..41) {
            slots[i].text = ""
            slots[i].setBackgroundResource(0)
        }

        //clearing textViews
        if (leaveTextViewNum != 0) {
            calendarLayout.removeView(leavesinMonth[0])
            if (leaveTextViewNum >= 2)
                calendarLayout.removeView(leavesinMonth[1])
            for (leave in leavesinMonth) {
                calendarLayout.removeView(leave)
            }
            leavesinMonth.clear()
        }
        leaveTextViewNum = 0

        if (dutyTextViewNum != 0) {
            for (duty in dutiesinMonth)
                calendarLayout.removeView(duty)
            dutiesinMonth.clear()
        }
        dutyTextViewNum = 0

        if (exerciseTextViewNum != 0) {
            calendarLayout.removeView(exercisesinMonth[0])
            if (exerciseTextViewNum >= 2)
                calendarLayout.removeView(exercisesinMonth[1])
            for (exercise in exercisesinMonth) {
                calendarLayout.removeView(exercise)
            }
            exercisesinMonth.clear()
        }
        exerciseTextViewNum = 0

        if (personalTextViewNum != 0) {
            for (duty in personalinMonth)
                calendarLayout.removeView(duty)
            personalinMonth.clear()
        }
        personalTextViewNum = 0

        //cloning just in case
        val cal = calendar.clone() as Calendar

        //putting numbers for month
        var month = cal.get(Calendar.MONTH)
        textMonth.text = "${month + 1}월"
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val init = cal.get(Calendar.DAY_OF_WEEK) - 1
        var position = init
        startSlot = init

        //calculating last day
        val cal2 = cal.clone() as Calendar
        cal2.add(Calendar.MONTH, 1)
        cal2.add(Calendar.DAY_OF_MONTH, -1)
        val j = cal2.get(Calendar.DAY_OF_MONTH) - 1

        //putting numbers for days
        for (i in 0..j) {
            slots[position].text = cal.get(Calendar.DAY_OF_MONTH).toString()
            position += 1
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // circle on today
        // https://stackoverflow.com/questions/25203501/android-creating-a-circular-textview
        val today = Calendar.getInstance()
        if (cal2.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal2.get(Calendar.MONTH) == today.get(
                Calendar.MONTH
            )
        ) {
            val todayposition = today.get(Calendar.DAY_OF_MONTH)
            slots[todayposition].setBackgroundResource(R.drawable.rounded_textview)
        }

        val cal3 = cal2.clone() as Calendar
        cal2.set(Calendar.DAY_OF_MONTH, 15)
        cal3.set(Calendar.DAY_OF_MONTH, 19)
        addLeaveinCalendar(cal2, cal3, "영덩이 찰싹")
        cal3.set(Calendar.DAY_OF_MONTH, 18)
        addExerciseinCalendar(cal2, cal3, "유격 훈련")

        cal2.set(Calendar.DAY_OF_MONTH, 16)
        addDutyinCalendar(cal2, "당직")
        cal2.set(Calendar.DAY_OF_MONTH, 8)
        addDutyinCalendar(cal2, "4시 불침번")
        cal2.set(Calendar.DAY_OF_MONTH, 1)
        cal3.set(Calendar.DAY_OF_MONTH, 12)
        addExerciseinCalendar(cal2, cal3, "전술작전훈련")

        addPersonalinCalendar(cal2, "후임 생일")
        cal2.set(Calendar.DAY_OF_MONTH, 24)
        addPersonalinCalendar(cal2, "100일!")
        cal2.set(Calendar.DAY_OF_YEAR, 28)
        addPersonalinCalendar(cal2, "티켓 예매!")

    }

    private fun addLeaveinCalendar(startDate: Calendar, endDate: Calendar, text: String) {
        val startPosition = startDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1
        val endPosition = endDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        if ((startPosition) / 7 == (endPosition) / 7)
            drawLeaveTextView(startPosition, endPosition, text)
        else {
            drawLeaveTextView(startPosition, (startPosition / 7) * 7 + 6, text)
            for (i in (startPosition / 7) + 1..(endPosition / 7) - 1)
                drawLeaveTextView(i * 7, i * 7 + 6, text)
            drawLeaveTextView((endPosition / 7) * 7, endPosition, text)
        }
    }

    private fun addDutyinCalendar(date: Calendar, text: String) {
        var position = date.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        val constraintSet = ConstraintSet()
        dutiesinMonth.add(TextView(this.context))
        dutiesinMonth[dutyTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(
            dutiesinMonth[dutyTextViewNum].id,
            ConstraintSet.START,
            slots[position].id,
            ConstraintSet.START
        )
        constraintSet.connect(
            dutiesinMonth[dutyTextViewNum].id,
            ConstraintSet.END,
            slots[position].id,
            ConstraintSet.END
        )
        constraintSet.connect(
            dutiesinMonth[dutyTextViewNum].id,
            ConstraintSet.BOTTOM,
            slots[position].id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            dutiesinMonth[dutyTextViewNum].id,
            ConstraintSet.TOP,
            slots[position].id,
            ConstraintSet.TOP
        )
        constraintSet.constrainWidth(dutiesinMonth[dutyTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(dutiesinMonth[dutyTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setVerticalBias(dutiesinMonth[dutyTextViewNum].id, 0.50f)
        dutiesinMonth[dutyTextViewNum].setBackgroundColor(Color.parseColor("#64B5F6"))
        dutiesinMonth[dutyTextViewNum].text = text
        dutiesinMonth[dutyTextViewNum].textSize = 8.0f
        calendarLayout.addView(dutiesinMonth[dutyTextViewNum])
        constraintSet.applyTo(calendarLayout)
        dutyTextViewNum++
    }

    fun addPersonalinCalendar(date: Calendar, text: String) {
        var position = date.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        val constraintSet = ConstraintSet()
        personalinMonth.add(TextView(this.context))
        personalinMonth[personalTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(
            personalinMonth[personalTextViewNum].id,
            ConstraintSet.START,
            slots[position].id,
            ConstraintSet.START
        )
        constraintSet.connect(
            personalinMonth[personalTextViewNum].id,
            ConstraintSet.END,
            slots[position].id,
            ConstraintSet.END
        )
        constraintSet.connect(
            personalinMonth[personalTextViewNum].id,
            ConstraintSet.BOTTOM,
            slots[position].id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            personalinMonth[personalTextViewNum].id,
            ConstraintSet.TOP,
            slots[position].id,
            ConstraintSet.TOP
        )
        constraintSet.constrainWidth(personalinMonth[personalTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(personalinMonth[personalTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setVerticalBias(personalinMonth[personalTextViewNum].id, 0.80f)
        personalinMonth[personalTextViewNum].setBackgroundColor(Color.parseColor("#E57373"))
        personalinMonth[personalTextViewNum].text = text
        personalinMonth[personalTextViewNum].textSize = 8.0f
        calendarLayout.addView(personalinMonth[personalTextViewNum])
        constraintSet.applyTo(calendarLayout)
        personalTextViewNum++
    }

    private fun addExerciseinCalendar(startDate: Calendar, endDate: Calendar, text: String) {
        var startPosition = startDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1
        var endPosition = endDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        if ((startPosition) / 7 == (endPosition) / 7)
            drawExerciseTextView(startPosition, endPosition, text)
        else {
            drawExerciseTextView(startPosition, (startPosition / 7) * 7 + 6, text)
            for (i in (startPosition / 7) + 1..(endPosition / 7) - 1)
                drawExerciseTextView(i * 7, i * 7 + 6, text)
            drawExerciseTextView((endPosition / 7) * 7, endPosition, text)
        }
    }

    //drawing Leave Text Views on fragment_calendar2 programmatically
    private fun drawLeaveTextView(startPos: Int, endPos: Int, text: String) {
        val constraintSet = ConstraintSet()
        leavesinMonth.add(TextView(this.context))
        leavesinMonth[leaveTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(
            leavesinMonth[leaveTextViewNum].id,
            ConstraintSet.START,
            slots[startPos].id,
            ConstraintSet.START
        )
        constraintSet.connect(
            leavesinMonth[leaveTextViewNum].id,
            ConstraintSet.END,
            slots[endPos].id,
            ConstraintSet.END
        )
        constraintSet.connect(
            leavesinMonth[leaveTextViewNum].id,
            ConstraintSet.BOTTOM,
            slots[startPos].id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            leavesinMonth[leaveTextViewNum].id,
            ConstraintSet.TOP,
            slots[endPos].id,
            ConstraintSet.TOP
        )
        constraintSet.constrainWidth(leavesinMonth[leaveTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(leavesinMonth[leaveTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setVerticalBias(leavesinMonth[leaveTextViewNum].id, 0.35f)
        leavesinMonth[leaveTextViewNum].setBackgroundColor(Color.parseColor("#7CB342"))
        leavesinMonth[leaveTextViewNum].text = text
        leavesinMonth[leaveTextViewNum].textSize = 8.0f
        calendarLayout.addView(leavesinMonth[leaveTextViewNum])
        constraintSet.applyTo(calendarLayout)
        leaveTextViewNum++
    }

    private fun drawExerciseTextView(startPos: Int, endPos: Int, text: String) {
        val constraintSet = ConstraintSet()
        exercisesinMonth.add(TextView(this.context))
        exercisesinMonth[exerciseTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(
            exercisesinMonth[exerciseTextViewNum].id,
            ConstraintSet.START,
            slots[startPos].id,
            ConstraintSet.START
        )
        constraintSet.connect(
            exercisesinMonth[exerciseTextViewNum].id,
            ConstraintSet.END,
            slots[endPos].id,
            ConstraintSet.END
        )
        constraintSet.connect(
            exercisesinMonth[exerciseTextViewNum].id,
            ConstraintSet.BOTTOM,
            slots[startPos].id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            exercisesinMonth[exerciseTextViewNum].id,
            ConstraintSet.TOP,
            slots[endPos].id,
            ConstraintSet.TOP
        )
        constraintSet.constrainWidth(exercisesinMonth[exerciseTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(exercisesinMonth[exerciseTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setVerticalBias(exercisesinMonth[exerciseTextViewNum].id, 0.65f)
        exercisesinMonth[exerciseTextViewNum].setBackgroundColor(Color.parseColor("#9E9D24"))
        exercisesinMonth[exerciseTextViewNum].text = text
        exercisesinMonth[exerciseTextViewNum].textSize = 8.0f
        calendarLayout.addView(exercisesinMonth[exerciseTextViewNum])
        constraintSet.applyTo(calendarLayout)
        exerciseTextViewNum++
    }
}
