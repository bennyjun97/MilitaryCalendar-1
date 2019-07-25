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
import com.kyminbb.militarycalendar.database.TableReaderForEachDays
import com.kyminbb.militarycalendar.utils.DateCalc
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_calendar2.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.find
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*


class CalendarFragment : Fragment() {

    private var adding = true
    private var calendar = LocalDate.now()
    private val today = LocalDate.now()

    var slots: Array<Button> = arrayOf()
    var startSlot = 0
    var eventTextViewNum = 0
    var leaveExist : MutableList<Boolean> = mutableListOf()

    var eventsinMonth : MutableList<TextView> = mutableListOf<TextView>()


    var daySelected = -1
    var selectedDate = LocalDate.now()


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

        updateCalendar(today, dbHelper)

        buttonAdd.setOnClickListener {
            if (adding) {
                addTab.visibility = View.VISIBLE
            } else {
                addTab.visibility = View.GONE
            }
            adding = !adding
        }

        buttonRight.setOnClickListener {
            calendar = calendar.withDayOfMonth(1)
            calendar = calendar.plusMonths(1)
            updateCalendar(calendar, dbHelper)
        }

        buttonLeft.setOnClickListener {
            calendar = calendar.withDayOfMonth(1)
            calendar = calendar.minusMonths(1)
            updateCalendar(calendar, dbHelper)
        }

        for (button in slots) {
            button.setOnClickListener {
                addTab.visibility = View.GONE
                adding = true
                if (button.text.isNotEmpty()) {
                    button.setBackgroundResource(R.drawable.calendar_stroke)
                    if (daySelected == -1 || daySelected == Integer.parseInt(button.text.toString()) + startSlot - 1) {
                        daySelected = Integer.parseInt(button.text.toString()) + startSlot - 1
                    } else {
                        slots[daySelected].setBackgroundResource(R.drawable.calendar_button)
                        daySelected = Integer.parseInt(button.text.toString()) + startSlot - 1
                    }
                    textDate.text = "${calendar.monthValue}월 ${button.text.toString()}일"
                    val readDate = readDBEachDate(dbHelper, date2String(calendar.year, calendar.monthValue, button.text.toString().toInt()))
                    if(readDate.isEmpty()) {
                        dayTypeText.text = "일정 없음"
                        commentText.text = ""
                    }
                    else {
                        for(date in readDate) {
                            dayTypeText.text = date.third
                            commentText.text = date.first
                        }
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
            addDuty(dbHelper)
        }

        addExercise.setOnClickListener {
            addExercise(dbHelper)
        }

        addPersonal.setOnClickListener {
            addPersonal(dbHelper)
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

    //휴가 일수 자동 계산, startSchedule 입력 했을 때 endSchedule 자동 계산
    private fun setDate(view: View, type: String, leaveType: String?) {
        val startSchedule = view.find<Button>(R.id.startSchedule)
        val endSchedule = view.find<Button>(R.id.endSchedule)
        var selected = false
        val date : LocalDate = when(type) {
            "Start" -> when {
                endSchedule.text.isEmpty() -> LocalDate.now()
                startSchedule.text.isNotEmpty() -> string2Date(startSchedule.text.toString())
                else -> when(leaveType) {
                    "휴가", "당직", "훈련", "개인" -> string2Date(endSchedule.text.toString())
                    else -> string2Date(endSchedule.text.toString()).minusDays(1)
                }
            }
            else -> when {
                startSchedule.text.isEmpty() -> LocalDate.now()
                endSchedule.text.isNotEmpty() -> string2Date(endSchedule.text.toString())
                else -> when(leaveType) {
                    "휴가", "당직", "훈련", "개인" -> string2Date(startSchedule.text.toString())
                    else -> string2Date(startSchedule.text.toString()).plusDays(1)
                }
            }
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            when(leaveType) {
                "휴가" -> when (type) {
                    "Start" -> when {
                        endSchedule.text.isEmpty() -> {
                            startSchedule.text = date2String(year, month + 1, day)
                        }
                        //endSchedule에 이미 날짜가 기입되어있을 경우 spinnerDatePicker에서 날짜를 고르면 그거에 따라서 자동으로 휴가 길이를 계산해준다.
                        else -> {
                            val actualDate = view.find<Button>(R.id.actualDayBtn)
                            startSchedule.text = date2String(year, month + 1, day)
                            actualDate.text = DateCalc.leaveDaysCalculator(
                                LocalDate.of(year, month + 1, day),
                                string2Date(endSchedule.text.toString())
                            ).toString() + "일"
                        }
                    }
                    "End" -> when {
                        startSchedule.text.isEmpty() -> {
                            endSchedule.text = date2String(year, month + 1, day)
                        }
                        //startSchedule에 이미 날짜가 기입되어있을 경우 spinnerDatePicker에서 날짜를 고르면 그거에 따라서 자동으로 휴가 길이를 계산해준다.
                        else -> {
                            val actualDate = view.find<Button>(R.id.actualDayBtn)
                            endSchedule.text = date2String(year, month + 1, day)
                            actualDate.text = DateCalc.leaveDaysCalculator(
                                string2Date(startSchedule.text.toString()),
                                LocalDate.of(year, month + 1, day)
                            ).toString() + "일"
                        }
                    }
                }

                "외박" -> when(type){
                    "Start" -> when {
                        endSchedule.text.isNotEmpty() -> startSchedule.text = date2String(year, month+1, day)
                        else -> {
                            startSchedule.text = date2String(year, month+1, day)
                            endSchedule.text = string2Date(date2String(year, month+1, day)).plusDays(1).toString()
                        }
                    }
                    "End" -> when {
                        startSchedule.text.isNotEmpty() -> endSchedule.text = date2String(year, month + 1, day)
                        else -> {
                            endSchedule.text = date2String(year, month+1, day)
                            startSchedule.text = string2Date(date2String(year, month+1, day)).plusDays(-1).toString()
                        }
                    }
                }

                "당직", "개인" -> when(type){
                    "Start" -> when {
                        endSchedule.text.isNotEmpty() -> startSchedule.text = date2String(year, month+1, day)
                        else -> {
                            startSchedule.text = date2String(year, month+1, day)
                            endSchedule.text = date2String(year, month+1, day)
                        }
                    }
                    "End" -> when {
                        startSchedule.text.isNotEmpty() -> endSchedule.text = date2String(year, month + 1, day)
                        else -> {
                            endSchedule.text = date2String(year, month+1, day)
                            startSchedule.text = date2String(year, month + 1, day)
                        }
                    }
                }
                else -> when(type) {
                    "Start" -> startSchedule.text = date2String(year, month + 1, day)
                    "End" -> endSchedule.text = date2String(year, month + 1, day)
                }
            }
        }
        var dialog = SpinnerDatePickerDialogBuilder()
            .context(context)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(today.year + 4, 11, 31)
            .minDate(today.year - 5, 0, 1)
            .defaultDate(date.year, date.monthValue - 1, date.dayOfMonth)

        if(selected)
            dialog = dialog.defaultDate(date.year, date.monthValue-1, date.dayOfMonth)
        dialog.build().show()
    }

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

    private fun writeDBEachDay(dbHelper: DBHelper, content: String, date: String, name: String, memo: String) {
        dbHelper.use {
            insert(
                TableReaderForEachDays.TableEntry2.TABLE_NAME,
                TableReaderForEachDays.TableEntry2.COLUMN_DATE to date,
                TableReaderForEachDays.TableEntry2.COLUMN_NAME to name,
                TableReaderForEachDays.TableEntry2.COLUMN_CONTENT to content,
                TableReaderForEachDays.TableEntry2.COLUMN_MEMO to memo
            )
        }
    }

    private fun writeDBEachDay(dbHelper: DBHelper, startDate: LocalDate, endDate: LocalDate, content: String, name: String, memo: String) {
        var date1 = startDate
        for(i in 0..DateCalc.leaveDaysCalculator(startDate, endDate) - 1) {
            writeDBEachDay(dbHelper, content, date2String(date1.year, date1.monthValue, date1.dayOfMonth), name, memo)
            date1 = date1.plusDays(1)
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

    //to read db for each days
    private fun readDBEachDate(dbHelper: DBHelper, date: String) : List<Triple<String, String, String>> {
        return dbHelper.use {
            select(
                TableReaderForEachDays.TableEntry2.TABLE_NAME,
                TableReaderForEachDays.TableEntry2.COLUMN_NAME,
                TableReaderForEachDays.TableEntry2.COLUMN_MEMO,
                TableReaderForEachDays.TableEntry2.COLUMN_CONTENT
            )
                .whereSimple("${TableReaderForEachDays.TableEntry2.COLUMN_DATE} = ?", date).exec {
                    val parser = rowParser { name: String, memo: String, content: String ->
                        Triple(name, memo, content)
                    }
                    parseList(parser)
                }
        }
    }

    //search according to end Date (월을 넘어가는 일정을 달력에 표시하기 위해서)
    private fun readDBEndDate(dbHelper: DBHelper, endDate: String): List<Triple<String, String, String>> {
        return dbHelper.use {
            select(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_START_DATE,
                TableReaderContract.TableEntry.COLUMN_CONTENT,
                TableReaderContract.TableEntry.COLUMN_MEMO
            )
                .whereSimple("${TableReaderContract.TableEntry.COLUMN_END_DATE} = ?", endDate).exec {
                    val parser = rowParser { startDate: String, content: String, memo: String ->
                        Triple(startDate, content, memo)
                    }
                    parseList(parser)
                }
        }
    }


    private fun date2String(year: Int, month: Int, dayOfMonth: Int): String {
        return "$year-${"%02d".format(month)}-${"%02d".format(dayOfMonth)}"
    }

    /*@SuppressLint("SimpleDateFormat")
    private fun string2Date(date: String): Calendar {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val cal = Calendar.getInstance()
        cal.time = format.parse(date)
        return cal
    }*/

    @SuppressLint("SimpleDateFormat")
    private fun string2Date(date: String): LocalDate {
       return LocalDate.parse(date)
    }

    private fun updateCalendar(calendar: LocalDate, dbHelper: DBHelper) {
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
        var cal = calendar

        //putting numbers for month
        var month = cal.monthValue
        textMonth.text = "${month}월"
        cal = cal.withDayOfMonth(1)
        val init = cal.dayOfWeek.value % 7
        var position = init
        startSlot = init

        //calculating last day
        var cal2 = cal
        cal2 = cal2.withDayOfMonth(1)
        cal2 = cal2.plusMonths(1)
        cal2 = cal2.minusDays(1)
        var j = cal2.dayOfMonth - 1


        //putting numbers for days
        for (i in 0..j) {
            leaveExist.add(false)
            slots[position].text = cal.dayOfMonth.toString()
            position += 1
            cal = cal.plusDays(1)
        }

        // circle on today
        // https://stackoverflow.com/questions/25203501/android-creating-a-circular-textview

        val today = LocalDate.now()
        if (cal2.year == today.year && cal2.monthValue == today.monthValue) {
            val todayposition = today.dayOfMonth
            slots[todayposition].setBackgroundResource(R.drawable.rounded_textview)
        }

       drawEventsinMonth(cal.minusDays(1), dbHelper)
    }

    private fun addEventinCalendar(startDate: LocalDate, endDate: LocalDate, type: String, text: String) {
        var startPosition = startDate.dayOfMonth + startSlot - 1
        var endPosition = endDate.dayOfMonth + startSlot - 1
        if(calendar.year != startDate.year || calendar.monthValue != startDate.monthValue) {
            if(calendar.year != endDate.year || calendar.monthValue != endDate.monthValue) {
                return
            }
            else {
                startPosition = startSlot
            }
        }
        else {
            if (endDate.monthValue != startDate.monthValue) {
                endPosition = startDate.withDayOfMonth(1).plusMonths(1).minusDays(1).dayOfMonth + startSlot - 1
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
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener { setDate(popupView, "Start", "휴가") }

        endSchedule.setOnClickListener { setDate(popupView, "End", "휴가") }

        nameInput.setOnClickListener { }

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
            if(nameInput.text.isEmpty()) memo = "휴가"
            else memo = nameInput.text.toString()
            if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
                Toast.makeText(this.context, "복귀 안 할거에요?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                    Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, "휴가")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), "휴가", memo)
                    writeDB(dbHelper, "휴가", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                    //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                        "휴가", memo, memo)
                }

                startSchedule.text = ""
                endSchedule.text = ""
                actualDateUsed.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }
        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null
            actualDateUsed.text = ""; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            actualDateUsed.text = ""
            endSchedule.text = ""
            nameInput.text = null
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
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

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
            if (nameInput.text.isEmpty()) memo = "휴가"
            else memo = nameInput.text.toString()
            if (startSchedule.text.isNotEmpty()) {
                // add event in calendar
                addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), "휴가", memo)
                // Store the schedule.
                // Store the schedule.
                // Store the schedule.
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), dbHelper, "휴가")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), "휴가", memo)
                    writeDB(dbHelper, "휴가", startSchedule.text.toString(), startSchedule.text.toString(), memo)
                    //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()),
                        "휴가", memo, memo)
                }
                startSchedule.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        nameInput.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            nameInput.text = null
            popup.dismiss()
        }
    }

    private fun addPass(dbHelper: DBHelper) {
        val popupView = layoutInflater.inflate(R.layout.add_pass, null)
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
        val buttonCancel = popupView.find<Button>(R.id.passCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.passInitBtn)
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {setDate(popupView, "Start", "외박")}
        endSchedule.setOnClickListener { setDate(popupView, "End", "외박") }

        buttonAddEvent.setOnClickListener {
            if (nameInput.text.isEmpty()) memo = "휴가"
            else memo = nameInput.text.toString()
            if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
                Toast.makeText(this.context, "복귀 안 할거에요?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                    Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Store the schedule.
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, "휴가")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), "휴가", memo)
                    writeDB(dbHelper, "휴가", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                    //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                        "휴가", memo, memo)
                }
                startSchedule.text = ""
                endSchedule.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        nameInput.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            nameInput.text = null; endSchedule.text = ""
            popup.dismiss()
        }
    }

    private fun addDuty(dbHelper: DBHelper) { //addPass랑 묶기
        val popupView = layoutInflater.inflate(R.layout.add_pass, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        changeTexts(popupView, "당직")
        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val endSchedule = popupView.find<Button>(R.id.endSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.passCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.passInitBtn)
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {setDate(popupView, "Start", "당직")}
        endSchedule.setOnClickListener { setDate(popupView, "End", "당직") }

        buttonAddEvent.setOnClickListener {
            if (nameInput.text.isEmpty()) memo = "당직"
            else memo = nameInput.text.toString()
            if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
            Toast.makeText(this.context, "퇴근 안 할거에요?", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                    Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Store the schedule.
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, "당직")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), "당직", memo)
                    writeDB(dbHelper, "당직", startSchedule.text.toString(), endSchedule.text.toString(), memo)

                    //각 날짜에 당직일정이 있다는 것을 기록해준다.
                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                        "당직", memo, memo)
                }
                startSchedule.text = ""
                endSchedule.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        nameInput.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            nameInput.text = null; endSchedule.text = ""
            popup.dismiss()
        }
    }

    private fun addExercise(dbHelper: DBHelper) { //addPass랑 묶기
        val popupView = layoutInflater.inflate(R.layout.add_pass, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        changeTexts(popupView, "훈련")
        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val endSchedule = popupView.find<Button>(R.id.endSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.passCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.passInitBtn)
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {setDate(popupView, "Start", "훈련")}
        endSchedule.setOnClickListener { setDate(popupView, "End", "훈련") }

        buttonAddEvent.setOnClickListener {
            if (nameInput.text.isEmpty()) memo = "훈련"
            else memo = nameInput.text.toString()
            if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
                Toast.makeText(this.context, "부대 안 돌아오고 /n 훈련지에만 있게요?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                    Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Store the schedule.
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, "훈련")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), "훈련", memo)
                    writeDB(dbHelper, "훈련", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                    //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                        "훈련", memo, memo)
                }

                startSchedule.text = ""
                endSchedule.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        nameInput.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            nameInput.text = null; endSchedule.text = ""
            popup.dismiss()
        }
    }

    private fun addPersonal(dbHelper: DBHelper) { //addPass랑 묶기
        val popupView = layoutInflater.inflate(R.layout.add_pass, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        changeTexts(popupView, "개인일정")
        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val endSchedule = popupView.find<Button>(R.id.endSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.passCancelBtn)
        val buttonInit = popupView.find<Button>(R.id.passInitBtn)
        val nameInput = popupView.find<EditText>(R.id.nameEdit)

        var memo = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        startSchedule.setOnClickListener {setDate(popupView, "Start", "개인")}
        endSchedule.setOnClickListener { setDate(popupView, "End", "개인") }

        buttonAddEvent.setOnClickListener {
            if (nameInput.text.isEmpty()) memo = "개인"
            else memo = nameInput.text.toString()
            if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
                Toast.makeText(this.context, "시작이 있으면... 끝이 있어야죠", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                    Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Store the schedule.
                if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, "개인")) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), "개인", memo)
                    writeDB(dbHelper, "개인", startSchedule.text.toString(), endSchedule.text.toString(), memo)
                    //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                    writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                        "개인", memo, memo)
                }
                startSchedule.text = ""
                endSchedule.text = ""
                nameInput.text = null
                // Dismiss the popup window.
                popup.dismiss()
            }
        }

        nameInput.setOnClickListener { }

        buttonInit.setOnClickListener {
            startSchedule.text = ""; nameInput.text = null; endSchedule.text = ""
        }
        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            startSchedule.text = ""
            nameInput.text = null; endSchedule.text = ""
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


    //initialization for updateCalendar
    private fun drawEventsinMonth(date: LocalDate, dbHelper: DBHelper) {
        var date1 = date.withDayOfMonth(1)

        for(i in 1..date.withDayOfMonth(1).plusMonths(1).minusDays(1).dayOfMonth) {
            val startDates = readDB(dbHelper, date2String(date1.year, date1.monthValue, date1.dayOfMonth))
            if(startDates.isNotEmpty()) {
                for(startDate in startDates) {
                    addEventinCalendar(date1, string2Date(startDate.first), startDate.second, startDate.third)
                }
            }

            val endDates = readDBEndDate(dbHelper, date2String(date1.year, date1.monthValue, date1.dayOfMonth))
            if(endDates.isNotEmpty()) {
                for(endDate in endDates) {
                    if(string2Date(endDate.first).monthValue != date1.monthValue) {
                        addEventinCalendar(date1.withDayOfMonth(1), date1, endDate.second, endDate.third)
                    }
                }
            }
            date1 = date1.plusDays(1)
        }
    }

    //시작일, 끝 일을 넣으면 그 사이에 특정 타입의 이벤트가 있는지 알려주는 함수.
    private fun doesEventExist(startDate: LocalDate, endDate: LocalDate, dbHelper: DBHelper, type: String) : Boolean {
        var date1 = startDate
        for(i in 0..DateCalc.leaveDaysCalculator(startDate, endDate)-1) {
            val dates = readDBEachDate(dbHelper, date2String(date1.year, date1.monthValue, date1.dayOfMonth))
            if(dates.isNotEmpty()) {
                for(date in dates) {
                    if(date.second.equals(type)) {
                        Toast.makeText(this.context, "이미 그 날짜에 그 일정이 있네요 ㅠㅠ", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
            }
            date1 = date1.plusDays(1)
        }
        return false
    }
}
