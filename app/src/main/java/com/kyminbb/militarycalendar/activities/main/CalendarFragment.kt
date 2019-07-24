package com.kyminbb.militarycalendar.activities.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.commit451.addendum.threetenabp.toLocalDate
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.database.DBHelper
import com.kyminbb.militarycalendar.database.TableReaderContract
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_calendar2.*
import org.jetbrains.anko.backgroundColor
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
    var eventTextViewNum = 0
    var leaveExist : MutableList<Boolean> = mutableListOf()
    var eventsinMonth : MutableList<TextView> = mutableListOf<TextView>()
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
                addTab.visibility = View.GONE
                adding = true
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
            addLeaveMenu(dbHelper)
        }

        addDuty.setOnClickListener {
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
                    writeDB(dbHelper, "당직", startSchedule.text.toString(), endSchedule.text.toString(), "당직")
                    // Retrieve the data for test purpose.
                    val endDates = readDB(dbHelper, startSchedule.text.toString())
                    for (endDate in endDates) {
                        addEventinCalendar(
                            string2Date(startSchedule.text.toString()),
                            string2Date(endDate.first),
                            "당직",
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

        addExercise.setOnClickListener {
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
                    writeDB(dbHelper, "훈련", startSchedule.text.toString(), endSchedule.text.toString(), "유격훈련")
                    // Retrieve the data for test purpose.
                    val endDates = readDB(dbHelper, startSchedule.text.toString())
                    for (endDate in endDates) {
                        addEventinCalendar(
                            string2Date(startSchedule.text.toString()),
                            string2Date(endDate.first),
                            "훈련",
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

        addPersonal.setOnClickListener {
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
                setDate(popupView, "Start", "")
            }

            endSchedule.setOnClickListener {
                setDate(popupView, "End", "")
            }

            buttonAddEvent.setOnClickListener {
                if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                    // Store the schedule.
                    writeDB(dbHelper, "개인", startSchedule.text.toString(), endSchedule.text.toString(), "소개팅")
                    // Retrieve the data for test purpose.
                    val endDates = readDB(dbHelper, startSchedule.text.toString())
                    for (endDate in endDates) {
                        addEventinCalendar(
                            string2Date(startSchedule.text.toString()),
                            string2Date(endDate.first),
                            "개인",
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

    //실제 휴가 일수 기본값을 자동으로 보여주기 위한 함수
    private fun setDate(view: View, type: String, leaveType: String?) {
        val startSchedule = view.find<Button>(R.id.startSchedule)
        val endSchedule = view.find<Button>(R.id.endSchedule)
        val actualDate = view.find<Button>(R.id.actualDayBtn)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            when (type) {
                "Start" -> when {
                    endSchedule.text.isEmpty() -> startSchedule.text = date2String(year, month + 1, day)
                    //endSchedule에 이미 날짜가 기입되어있을 경우 spinnerDatePicker에서 날짜를 고르면 그거에 따라서 자동으로 휴가 길이를 계산해준다.
                    else -> {
                        startSchedule.text = date2String(year, month + 1, day)
                        actualDate.text = (string2Date(endSchedule.text.toString()).get(Calendar.DAY_OF_YEAR)
                                - string2Date(startSchedule.text.toString()).get(Calendar.DAY_OF_YEAR) + 1).toString() + "일"
                    }
                }
                "End" -> when {
                    startSchedule.text.isEmpty() -> endSchedule.text = date2String(year, month + 1, day)
                    //startSchedule에 이미 날짜가 기입되어있을 경우 spinnerDatePicker에서 날짜를 고르면 그거에 따라서 자동으로 휴가 길이를 계산해준다.
                    else -> {
                        endSchedule.text = date2String(year, month + 1, day)
                        actualDate.text = (string2Date(endSchedule.text.toString()).get(Calendar.DAY_OF_YEAR)
                                - string2Date(startSchedule.text.toString()).get(Calendar.DAY_OF_YEAR) + 1).toString() + "일"
                    }
                }
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

        if (eventTextViewNum != 0) {
            calendarLayout.removeView(eventsinMonth[0])
            if (eventTextViewNum >= 2)
                calendarLayout.removeView(eventsinMonth[1])
            for (event in eventsinMonth) {
                calendarLayout.removeView(event)
            }
            eventsinMonth.clear()
        }
        eventTextViewNum = 0

        //clearing booleans
        leaveExist.clear()

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
            leaveExist.add(false)
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
    }

    private fun addEventinCalendar(startDate: Calendar, endDate: Calendar, type: String, text: String) {
        val startPosition = startDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1
        val endPosition = endDate.get(Calendar.DAY_OF_MONTH) + startSlot - 1

        for(index in startDate.get(Calendar.DAY_OF_MONTH)-1..endDate.get(Calendar.DAY_OF_MONTH)) {
            if(type.equals("휴가") && leaveExist[index]) {
                Toast.makeText(this.context, "휴가일이 겹칩니다!", Toast.LENGTH_SHORT).show() //나중엔 여기다 쓰면 안 된다. 실험용으로 여기에 씀.
                return
            }
        }

        if ((startPosition) / 7 == (endPosition) / 7)
            drawEventTextView(startPosition, endPosition, type, text)
        else {
            drawEventTextView(startPosition, (startPosition / 7) * 7 + 6, type, text)
            for (i in (startPosition / 7) + 1..(endPosition / 7) - 1)
                drawEventTextView(i * 7, i * 7 + 6, type, text)
            drawEventTextView((endPosition / 7) * 7, endPosition, type, text)
        }
    }

    private fun drawEventTextView(startPos: Int, endPos: Int, type: String, text: String) {

        val constraintSet = ConstraintSet()
        eventsinMonth.add(TextView(this.context))
        eventsinMonth[eventTextViewNum].id = View.generateViewId()
        constraintSet.clone(calendarLayout)
        constraintSet.connect(
            eventsinMonth[eventTextViewNum].id,
            ConstraintSet.START,
            slots[startPos].id,
            ConstraintSet.START
        )
        constraintSet.connect(
            eventsinMonth[eventTextViewNum].id,
            ConstraintSet.END,
            slots[endPos].id,
            ConstraintSet.END
        )
        constraintSet.connect(
            eventsinMonth[eventTextViewNum].id,
            ConstraintSet.BOTTOM,
            slots[startPos].id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            eventsinMonth[eventTextViewNum].id,
            ConstraintSet.TOP,
            slots[endPos].id,
            ConstraintSet.TOP
        )
        constraintSet.constrainWidth(eventsinMonth[eventTextViewNum].id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(eventsinMonth[eventTextViewNum].id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setMargin(eventsinMonth[eventTextViewNum].id, ConstraintSet.START, 4)
        constraintSet.setMargin(eventsinMonth[eventTextViewNum].id, ConstraintSet.END, 4)
        when(type) {
            "휴가" -> {constraintSet.setVerticalBias(eventsinMonth[eventTextViewNum].id, 0.35f)
                eventsinMonth[eventTextViewNum].setBackgroundResource(R.drawable.leave_textview_both_rounded) }
            "당직" -> {constraintSet.setVerticalBias(eventsinMonth[eventTextViewNum].id, 0.50f)
                eventsinMonth[eventTextViewNum].setBackgroundResource(R.drawable.duty_textview_both_rounded)}
            "훈련" -> {constraintSet.setVerticalBias(eventsinMonth[eventTextViewNum].id, 0.65f)
                eventsinMonth[eventTextViewNum].setBackgroundResource(R.drawable.exercise_textview_both_rounded)}
            else -> {constraintSet.setVerticalBias(eventsinMonth[eventTextViewNum].id, 0.80f)
                eventsinMonth[eventTextViewNum].setBackgroundResource(R.drawable.personal_textview_both_rounded)}
        }
        eventsinMonth[eventTextViewNum].text = text
        eventsinMonth[eventTextViewNum].textSize = 8.0f
        calendarLayout.addView(eventsinMonth[eventTextViewNum])
        constraintSet.applyTo(calendarLayout)
        eventTextViewNum++

        if(type.equals("휴가")) {
            for (index in startPos-startSlot..endPos-startSlot) {
                leaveExist[index] = true
            }
        }
    }

    private fun addLeaveMenu(dbHelper: DBHelper) {
        val addLeaveArray = resources.getStringArray(R.array.addLeave_string)
        val addLeaveButtons = arrayOf(
            R.id.LeaveSelect,
            R.id.PassSelect,
            R.id.offPostSelect
        )
        val popupMenu = PopupMenu(this.context, addLeave)
        popupMenu.menuInflater.inflate(R.menu.pass_leave_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when(addLeaveArray[addLeaveButtons.indexOf(it.itemId)]) {
                "휴가" -> addLeave(dbHelper)
                "외박" -> addPass(dbHelper)
                else -> addOffPost(dbHelper)
            }
            true
        }
        popupMenu.show()
    }

    private fun addLeave(dbHelper: DBHelper) {
        val popupView = layoutInflater.inflate(R.layout.add_leave, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val endSchedule = popupView.find<Button>(R.id.endSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.leaveCancelBtn)
        val actualDateUsed = popupView.find<Button>(R.id.actualDayBtn)
        val buttonInit = popupView.find<Button>(R.id.leaveInitBtn)
        val leaveMemo = popupView.find<EditText>(R.id.memoEdit)

        var memo = ""
        leaveMemo.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener { setDate(popupView, "Start", "leave") }

        endSchedule.setOnClickListener { setDate(popupView, "End", "leave") }

        leaveMemo.setOnClickListener { }

        actualDateUsed.setOnClickListener {
            val popupMenu = PopupMenu(this.context, addLeave)
            popupMenu.menuInflater.inflate(R.menu.actual_leave_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                actualDateUsed.text = it.title
                true
            }
            popupMenu.show()
        }

        buttonAddEvent.setOnClickListener {
            if(leaveMemo.text.isEmpty()) memo = "휴가"
            else memo = leaveMemo.text.toString()
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                // Store the schedule.
                writeDB(dbHelper, "휴가", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                // Retrieve the data for test purpose.
                val endDates = readDB(dbHelper, startSchedule.text.toString())
                for (endDate in endDates) {
                    addEventinCalendar(
                        string2Date(startSchedule.text.toString()),
                        string2Date(endDate.first),
                        "휴가",
                        endDate.third
                    )
                }
                startSchedule.text = ""
                endSchedule.text = ""
                actualDateUsed.text = ""
                leaveMemo.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }
        buttonInit.setOnClickListener {
            startSchedule.text = ""; leaveMemo.text = null
            actualDateUsed.text = ""; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            actualDateUsed.text = ""
            endSchedule.text = ""
            leaveMemo.text = null
            popup.dismiss()
        }
    }

    private fun addOffPost(dbHelper: DBHelper) {
        val popupView = layoutInflater.inflate(R.layout.add_offpost, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.offPostCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.offPostInitBtn)
        val leaveMemo = popupView.find<EditText>(R.id.memoEdit)

        var memo = ""
        leaveMemo.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                startSchedule.text = date2String(year, month + 1, day)
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

        buttonAddEvent.setOnClickListener {
            if (leaveMemo.text.isEmpty()) memo = "휴가"
            else memo = leaveMemo.text.toString()
            if (startSchedule.text.isNotEmpty()) {
                // Store the schedule.
                writeDB(dbHelper, "휴가", startSchedule.text.toString(), startSchedule.text.toString(), memo)
                // Retrieve the data for test purpose.
                val endDates = readDB(dbHelper, startSchedule.text.toString())
                for (endDate in endDates) {
                    addEventinCalendar(
                        string2Date(startSchedule.text.toString()),
                        string2Date(endDate.first),
                        "휴가",
                        endDate.third
                    )
                }
                startSchedule.text = ""
                leaveMemo.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        leaveMemo.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; leaveMemo.text = null
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            leaveMemo.text = null
            popup.dismiss()
        }
    }

    private fun addPass(dbHelper: DBHelper) {
        val popupView = layoutInflater.inflate(R.layout.add_pass, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = false
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val endSchedule = popupView.find<Button>(R.id.endSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.passCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.passInitBtn)
        val leaveMemo = popupView.find<EditText>(R.id.memoEdit)

        var memo = ""
        leaveMemo.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {setDate(popupView, "Start")}
        endSchedule.setOnClickListener { setDate(popupView, "End") }

        buttonAddEvent.setOnClickListener {
            if (leaveMemo.text.isEmpty()) memo = "휴가"
            else memo = leaveMemo.text.toString()
            if (startSchedule.text.isNotEmpty()) {
                // Store the schedule.
                writeDB(dbHelper, "휴가", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                // Retrieve the data for test purpose.
                val endDates = readDB(dbHelper, startSchedule.text.toString())
                for (endDate in endDates) {
                    addEventinCalendar(
                        string2Date(startSchedule.text.toString()),
                        string2Date(endDate.first),
                        "휴가",
                        endDate.third
                    )
                }
                startSchedule.text = ""
                leaveMemo.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        leaveMemo.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; leaveMemo.text = null; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            leaveMemo.text = null; endSchedule.text = ""
            popup.dismiss()
        }
    }

    private fun changeTexts(view: View, type: String) {
        val title = view.findViewById<TextView>(R.id.titleText)
        val startText = view.findViewById<TextView>(R.id.startText)
        val endText = view.findViewById<TextView>(R.id.endText)
        val registerBtn = view.findViewById<Button>(R.id.RegisterBtn)

        title.text = type + " 등록하기"
        startText.text = type + " 시작일"
        endText.text = type + " 종료일"
        registerBtn.text = type + " 등록"
    }
}
