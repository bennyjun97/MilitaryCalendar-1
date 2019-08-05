package com.kyminbb.militarycalendar.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.database.DBHelper
import com.kyminbb.militarycalendar.database.TableReaderContract
import com.kyminbb.militarycalendar.database.TableReaderForEachDays
import com.kyminbb.militarycalendar.utils.CalendarRvAdapter
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Event
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_calendar2.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.find
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*


class CalendarFragment : Fragment() {

    private var adding = true
    private var calendar = LocalDate.now()
    private val today = LocalDate.now()

    var slots: Array<Button> = arrayOf()
    var textSlots: Array<TextView> = arrayOf()
    var startSlot = 0
    var eventTextViewNum = 0
    var leaveExist : MutableList<Boolean> = mutableListOf()
    var eventsinMonth : MutableList<TextView> = mutableListOf<TextView>()
    var daySelected = -1
    var memoTyped = ""


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
            day1, day2, day3, day4, day5, day6, day7, day8, day9, day10, day11, day12, day13, day14,
            day15, day16, day17, day18, day19, day20, day21, day22, day23, day24, day25, day26, day27, day28,
            day29, day30, day31, day32, day33, day34, day35, day36, day37, day38, day39, day40, day41, day42
        )
        textSlots = arrayOf(
            dayText1, dayText2, dayText3, dayText4, dayText5, dayText6, dayText7, dayText8, dayText9,
            dayText10, dayText11, dayText12, dayText13, dayText14, dayText15, dayText16, dayText17,
            dayText18, dayText19, dayText20, dayText21, dayText22, dayText23, dayText24, dayText25,
            dayText26, dayText27, dayText28, dayText29, dayText30, dayText31, dayText32, dayText33,
            dayText34, dayText35, dayText36, dayText37, dayText38, dayText39, dayText40, dayText41, dayText42
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

        if(daySelected == -1) {
            textDate.text = "${today.monthValue}월 ${today.dayOfMonth}일"
            //read schedules on that day from db
            val readDate = readDBEachDate(
                dbHelper,
                date2String(today.year, today.monthValue, today.dayOfMonth)
            )
            if (readDate.isEmpty()) {
                dayTypeText.text = "일정 없음"
                commentText.text = ""
            } else {
                for (date in readDate) {
                    dayTypeText.text = date.third
                    commentText.text = date.first
                }
            }
        }

        for (i in 0..41) {
            slots[i].setOnClickListener {
                addTab.visibility = View.GONE
                adding = true
                if (textSlots[i].text.isNotEmpty()) {
                    slots[i].setBackgroundResource(R.drawable.calendar_stroke)
                    if (daySelected == -1) {
                        daySelected = Integer.parseInt(textSlots[i].text.toString()) + startSlot - 1
                    } else {
                        if (daySelected == Integer.parseInt(textSlots[i].text.toString()) + startSlot - 1) {
                            dayLayoutPopUp(
                                dbHelper,
                                date2String(
                                    calendar.year,
                                    calendar.monthValue,
                                    Integer.parseInt(textSlots[i].text.toString())
                                )
                            )
                        } else {
                            slots[daySelected].setBackgroundResource(R.drawable.calendar_button)
                            daySelected = Integer.parseInt(textSlots[i].text.toString()) + startSlot - 1
                        }
                    }
                    textDate.text = "${calendar.monthValue}월 ${textSlots[i].text.toString()}일"

                    //read schedules on that day from db
                    val readDate = readDBEachDate(
                        dbHelper,
                        date2String(calendar.year, calendar.monthValue, textSlots[i].text.toString().toInt())
                    )
                    if (readDate.isEmpty()) {
                        dayTypeText.text = "일정 없음"
                        commentText.text = ""
                    } else {
                        for (date in readDate) {
                            dayTypeText.text = date.third
                            commentText.text = date.first
                        }
                    }
                }
            }
        }

        addLeave.setOnClickListener {
            addLeaveMenu(dbHelper)
        }

        addDuty.setOnClickListener {
            addEvent(dbHelper, "당직", false)
        }

        addExercise.setOnClickListener {
            addEvent(dbHelper, "훈련", false)
        }

        addPersonal.setOnClickListener {
            addEvent(dbHelper, "개인", false)
        }

        buttonMenu.setOnClickListener {
            val wrapper = ContextThemeWrapper(this.context!!, R.style.calendarPopUp)
            val popupMenu = PopupMenu(wrapper, it)
            popupMenu.menuInflater.inflate(R.menu.calendar_setting_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener{
                when(it.itemId) {
                    R.id.searchItem -> Toast.makeText(this.context!!, "1", Toast.LENGTH_SHORT).show()
                    else -> {
                        it.setChecked(!it.isChecked)
                    }
                }
                false
            }
            popupMenu.show()
        }
    }

    //popup layout of add_offpost.xml does not have button by the name of endSchedule.
    //Thus, using setDate function below causes error, so I just made setDateOffPost function.
    private fun setDateOffPost(view: View) {
        val startSchedule = view.find<Button>(R.id.startSchedule)
        var selected = false
        val date : LocalDate = when {
                startSchedule.text.isNotEmpty() -> string2Date(startSchedule.text.toString())
                else -> LocalDate.now()
            }
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            startSchedule.text = date2String(year, month + 1, day)
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

    //휴가 일수 자동 계산, startSchedule 입력 했을 때 endSchedule 자동 계산. 여기서 해야 spinnerdatepicker에서 확인 누른 순간에 업데이트 된다.
    private fun setDate(view: View, type: String, leaveType: String?) {
        val startSchedule = view.find<Button>(R.id.startSchedule)
        val endSchedule = view.find<Button>(R.id.endSchedule)
        var selected = false
        val date : LocalDate = when(type) {
            "Start" -> when {
                endSchedule.text.isEmpty() -> LocalDate.now()
                startSchedule.text.isNotEmpty() -> string2Date(startSchedule.text.toString())
                else -> when (leaveType) {
                    "휴가", "당직", "훈련", "개인" -> string2Date(endSchedule.text.toString())
                    else -> string2Date(endSchedule.text.toString()).minusDays(1)
                }
            }
            else -> when {
                startSchedule.text.isEmpty() -> LocalDate.now()
                endSchedule.text.isNotEmpty() -> string2Date(endSchedule.text.toString())
                else -> when (leaveType) {
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

    //inserts information about an event into the first table of db.
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

    //inserts information oabout an event of a date into the second table of db.
    private fun writeDBEachDay(dbHelper: DBHelper, content: String, date: String, name: String, memo: String, startDate: String, endDate: String) {
        dbHelper.use {
            insert(
                TableReaderForEachDays.TableEntry2.TABLE_NAME,
                TableReaderForEachDays.TableEntry2.COLUMN_DATE to date,
                TableReaderForEachDays.TableEntry2.COLUMN_NAME to name,
                TableReaderForEachDays.TableEntry2.COLUMN_CONTENT to content,
                TableReaderForEachDays.TableEntry2.COLUMN_MEMO to memo,
                TableReaderForEachDays.TableEntry2.COLUMN_START to startDate,
                TableReaderForEachDays.TableEntry2.COLUMN_END to endDate
            )
        }
    }

    //insert information about every day within startdate and endDate to the db
    private fun writeDBEachDay(dbHelper: DBHelper, startDate: LocalDate, endDate: LocalDate, content: String, name: String, memo: String) {
        var date1 = startDate
        for(i in 0..DateCalc.leaveDaysCalculator(startDate, endDate) - 1) {
            writeDBEachDay(dbHelper, content, date2String(date1.year, date1.monthValue, date1.dayOfMonth), name, memo, date2String(startDate), date2String(endDate))
            date1 = date1.plusDays(1)
        }
    }

    //delete from db.
    private fun deleteDB(dbHelper: DBHelper, content: String, startDate: String, endDate: String, name: String, memo: String) {
        dbHelper.use {
            //delete from first table of db
            execSQL("delete from calendar where content='" + content + "' and start_date='" + startDate + "'")
            //delete from second table of db
            execSQL("delete from calendar2 where content2='" + content + "' and start_date2='" + startDate + "' and name2='" + name + "'")
        }
    }

    //returns List<endDate, content, memo> from startDate (first table of db)
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

    //to read db for each days. returns List<name, memo, content> from date
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

    //returns triple of content, startDate, endDate (used for deleting). able to find out content, startDate and endDate of all events of that date
    private fun readDBStartEndDates(dbHelper: DBHelper, date: String) : List<Triple<String, String, String>> {
        return dbHelper.use {
            select(
                TableReaderForEachDays.TableEntry2.TABLE_NAME,
                TableReaderForEachDays.TableEntry2.COLUMN_CONTENT,
                TableReaderForEachDays.TableEntry2.COLUMN_START,
                TableReaderForEachDays.TableEntry2.COLUMN_END
            )
                .whereSimple("${TableReaderForEachDays.TableEntry2.COLUMN_DATE} = ?", date).exec {
                    val parser = rowParser { content: String, startDate: String, endDate: String ->
                        Triple(content, startDate, endDate)
                    }
                    parseList(parser)
                }
        }
    }

    //search according to end Date (월을 넘어가는 일정을 달력에 표시하기 위해서). 예를 들어 8월 달력에 있는데 7월 28일~8월 2일의 일정이 있으면,
    //그 일정을 달력에 표시하기 위해서는 시작일이 아니라 끝일을 기준으로 검색해야 한다.
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

    private fun date2String(date: LocalDate): String {
        return "${date.year}-${"%02d".format(date.monthValue)}-${"%02d".format(date.dayOfMonth)}"
    }

    @SuppressLint("SimpleDateFormat")
    private fun string2Date(date: String): LocalDate {
       return LocalDate.parse(date)
    }

    // updating calendar whenever we click arrows or open the app
    private fun updateCalendar(calendar: LocalDate, dbHelper: DBHelper) {
        //clearing slots
        for (i in 0..41) {
            textSlots[i].text = ""
            textSlots[i].setBackgroundResource(0)
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
        var year = cal.year
        textMonth.text = "${month}월"
        textYear.text = "${year}"
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
            textSlots[position].text = cal.dayOfMonth.toString()
            position += 1
            cal = cal.plusDays(1)
        }

        // circle on today
        // https://stackoverflow.com/questions/25203501/android-creating-a-circular-textview

        val today = LocalDate.now()
        if (cal2.year == today.year && cal2.monthValue == today.monthValue) {
            val todayposition = today.dayOfMonth + init - 1
            textSlots[todayposition].setBackgroundResource(R.drawable.rounded_textview)
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
                "휴가" -> addEvent(dbHelper, "휴가", false)
                "외박" -> addEvent(dbHelper, "외박", false)
                else -> addEvent(dbHelper, "외출", false)
            }
            true
        }
        popupMenu.show()
    }

    private fun addEvent(dbHelper: DBHelper, type: String, editBoolean: Boolean) {
        val popupView : View;
        if(type.equals("휴가")) {
            popupView = layoutInflater.inflate(R.layout.add_leave, null)
        }
        else if(type.equals("외출")) {
            popupView = layoutInflater.inflate(R.layout.add_offpost, null)
        }
        else {
            popupView = layoutInflater.inflate(R.layout.add_pass, null)
        }
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        if(type.equals("개인")) changeTextsForAdding(popupView, "개인일정")
        else changeTextsForAdding(popupView, type)

        val startSchedule = popupView.find<Button>(R.id.startSchedule)
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.CancelBtn)
        val buttonInit = popupView.find<Button>(R.id.InitBtn)
        val nameInput = popupView.find<EditText>(R.id.nameEdit)
        val memoBtn = popupView.find<Button>(R.id.memoButton)

        startSchedule.text = ""
        nameInput.text = null

        if(!editBoolean) memoTyped = ""
        var name = ""
        nameInput.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)

        nameInput.setOnClickListener { }

        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            popup.dismiss()
        }

        memoBtn.setOnClickListener {
            memoPopUp(memoTyped)
        }


        if(type.equals("외출")) {
            buttonInit.setOnClickListener {
                startSchedule.text = ""; nameInput.text = null
            }

            startSchedule.setOnClickListener { setDateOffPost(popupView) }

            buttonAddEvent.setOnClickListener {
                if (nameInput.text.isEmpty()) name = "휴가"
                else name = nameInput.text.toString()
                if (startSchedule.text.isNotEmpty()) {
                    // add event in calendar
                    addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), "휴가", name)
                    if(doesEventExist(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), dbHelper, "휴가")) {
                        return@setOnClickListener
                    }
                    // Store the schedule.
                    else if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), dbHelper, "휴가")) {
                        // add event in calendar
                        addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), "휴가", name)
                        writeDB(dbHelper, "휴가", startSchedule.text.toString(), startSchedule.text.toString(), name)
                        //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                        writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()),
                            "휴가", name, memoTyped)
                    }
                    // Dismiss the popup window.
                    popup.dismiss()
                }
            }
        }

        else {
            val endSchedule = popupView.find<Button>(R.id.endSchedule)
            endSchedule.text = ""
            startSchedule.setOnClickListener { setDate(popupView, "Start", type) }
            endSchedule.setOnClickListener { setDate(popupView, "End", type) }

            buttonAddEvent.setOnClickListener {
                if(nameInput.text.isEmpty()) name = type
                else name = nameInput.text.toString()
                if(startSchedule.text.isNotEmpty() && endSchedule.text.isEmpty()) {
                    Toast.makeText(this.context, "복귀 안 할거에요?", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (startSchedule.text.isNotEmpty() && endSchedule.text.isNotEmpty()) {
                    val eventType: String
                    if(type.equals("외박")) eventType = "휴가"
                    else eventType = type
                    if(string2Date(startSchedule.text.toString()).isAfter(string2Date(endSchedule.text.toString()))) {
                        Toast.makeText(this.context, "복귀일이 시작일보다 앞이려면 \n 시간여행을 해야해요!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if(doesEventExist(string2Date(startSchedule.text.toString()), string2Date(startSchedule.text.toString()), dbHelper, eventType)) {
                        return@setOnClickListener
                    }
                    else if(!doesEventExist(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), dbHelper, eventType)) {
                        // add event in calendar
                        addEventinCalendar(string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()), eventType, name)
                        writeDB(dbHelper, eventType, startSchedule.text.toString(), endSchedule.text.toString(), name)
                        //각 날짜에 휴가일정이 있다는 것을 기록해준다.

                        writeDBEachDay(dbHelper, string2Date(startSchedule.text.toString()), string2Date(endSchedule.text.toString()),
                            eventType, name, memoTyped)
                    }
                    // Dismiss the popup window.
                    popup.dismiss()
                }
            }

            if(type.equals("휴가")) {
                val actualDateUsed = popupView.find<Button>(R.id.actualDayBtn)
                actualDateUsed.text = ""
                buttonInit.setOnClickListener {
                    startSchedule.text = ""; nameInput.text = null
                    actualDateUsed.text = ""; endSchedule.text = ""
                }
                actualDateUsed.setOnClickListener {
                    val popupMenu = PopupMenu(this.context, addLeave)
                    popupMenu.menuInflater.inflate(R.menu.actual_leave_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener {
                        actualDateUsed.text = it.title
                        true
                    }
                    popupMenu.show()
                }
            }
            else {
                buttonInit.setOnClickListener {
                    startSchedule.text = ""; nameInput.text = null
                    endSchedule.text = ""
                }
            }
        }
    }
    private fun changeTextsForAdding(view: View, type: String) {
        val title = view.find<TextView>(R.id.titleText)
        val startText = view.find<TextView>(R.id.startText)
        val registerBtn = view.find<Button>(R.id.RegisterBtn)

        title.text = type + " 등록하기"
        startText.text = type + " 시작일"
        registerBtn.text = type + " 등록"

        if(type.equals("외출")) return

        val endText = view.find<TextView>(R.id.endText)
        endText.text = type + " 종료일"
    }


    private fun changeScheduleTextsForEdit(view: View, type: String, startDate: String, endDate: String, name: String, actualDate: String) {
        val title = view.find<TextView>(R.id.titleText)
        val start = view.find<Button>(R.id.startSchedule)
        val nameText = view.find<EditText>(R.id.nameEdit)
        val memoBtn = view.find<Button>(R.id.memoButton)
        val cancel = view.find<Button>(R.id.CancelBtn)
        title.text = type + " 수정하기"
        start.text = startDate
        nameText.setText(name)
        memoBtn.text = "메모 수정하기"
        cancel.text = "수정 취소"

        if(type.equals("외출")) return

        val end = view.find<Button>(R.id.endSchedule)
        end.text = endDate

        if(type.equals("휴가")) {
            val actualDateBtn = view.find<Button>(R.id.actualDayBtn)
            actualDateBtn.text = actualDate + "일"
        }
    }

    private fun changeMemoTextsForEdit(view: View, memo: String) {
        val title = view.find<TextView>(R.id.titleText)
        val memoEdit = view.find<EditText>(R.id.memoEdit)
        val cancel = view.find<Button>(R.id.memoCancel)

        title.text = "메모 수정하기"
        cancel.text = "수정 취소"
        memoEdit.setText(memo)
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
                    if(date.third.equals(type)) {
                        Toast.makeText(this.context, "이미 그 날짜에 그 일정이 있네요 ㅠㅠ", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
            }
            date1 = date1.plusDays(1)
        }
        return false
    }

    private fun memoPopUp(memo: String) {
        val popupView = layoutInflater.inflate(R.layout.add_memo, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )
        val buttonAddEvent = popupView.find<Button>(R.id.RegisterBtn)
        val buttonCancel = popupView.find<Button>(R.id.memoCancel)
        val memoInput = popupView.find<EditText>(R.id.memoEdit)
        memoInput.setText(memo)
        var memoString = ""

        memoInput.setOnClickListener{}
        buttonAddEvent.setOnClickListener {
            if (memoInput.text.isEmpty()) memoString = ""
            else memoString = memoInput.text.toString()
            memoTyped = memoString

            memoInput.text = null
            // Dismiss the popup window.
            popup.dismiss()
        }

        buttonCancel.setOnClickListener {
            // Dismiss the popup window.
            memoInput.text = null
            memoTyped = memo
            popup.dismiss()
        }
    }

    //popup that appears when slot[index] pressed twice or for long
    private fun dayLayoutPopUp(dbHelper: DBHelper, date: String) {
        val popupView = layoutInflater.inflate(R.layout.calendar_day_layout, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        val buttonLeft = popupView.find<ImageButton>(R.id.LeftButton)
        val buttonRight = popupView.find<ImageButton>(R.id.RightButton)
        val dateText = popupView.find<TextView>(R.id.titleText)
        val recycler = popupView.find<RecyclerView>(R.id.calendarDayRecycler)

        dateText.text = date

        updateRecyclerView(dbHelper, activity!!.applicationContext, recycler, date)

        buttonLeft.setOnClickListener {
            dateText.text = date2String(string2Date(dateText.text.toString()).minusDays(1))
            updateRecyclerView(dbHelper, this.context!!, recycler, dateText.text.toString())
        }

        buttonRight.setOnClickListener {
            dateText.text = date2String(string2Date(dateText.text.toString()).plusDays(1))
            updateRecyclerView(dbHelper, this.context!!, recycler, dateText.text.toString())
        }
    }

    //date, content -> returns startDate
    private fun startDatefromDate(dbHelper: DBHelper, date: String, content: String) : String {
        val dates = readDBStartEndDates(dbHelper, date)
        if(date.isEmpty()) return ""
        for(date in dates) {
            if(date.first.equals(content)) {
                return date.second
            }
        }
        return ""
    }

    //date, content -> returns endDate
    private fun endDatefromDate(dbHelper: DBHelper, date: String, content: String) : String {
        val dates = readDBStartEndDates(dbHelper, date)
        if(date.isEmpty()) return ""
        for(date in dates) {
            if(date.first.equals(content)) {
                return date.third
            }
        }
        return ""
    }

    //load all event data of that date and returns an arraylist of Event
    fun loadEventData(dbHelper: DBHelper, date: String): ArrayList<Event> {
        val arrayList = ArrayList<Event>()

        val events = readDBEachDate(dbHelper, date)
        for(event in events) {
            var eventInput = Event("", "", "", "", "")
            eventInput.eventContent = event.third
            eventInput.eventName = event.first
            eventInput.eventMemo = event.second
            eventInput.eventStartDate = startDatefromDate(dbHelper, date, event.third)
            eventInput.eventEndDate = endDatefromDate(dbHelper, date, event.third)

            arrayList.add(eventInput)
        }
        return arrayList
    }

    //delete that event from calendar by calling deletedB function
    fun deleteEventData(dbHelper: DBHelper, event: Event) {
        deleteDB(dbHelper, event.eventContent, event.eventStartDate, event.eventEndDate, event.eventName, event.eventMemo)
    }

    //updates recyclerView that shows all the events of that date
    fun updateRecyclerView(dbHelper: DBHelper, context:Context, view: RecyclerView, date: String) {
        // add recylcerView
        val arrayEventList = loadEventData(dbHelper, date)
        val adapter = CalendarRvAdapter(activity!!.applicationContext, arrayEventList)
        view.adapter = adapter

        adapter.setOnItemClickListener(object : CalendarRvAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                val popupEditMenu = PopupMenu(context, v)
                popupEditMenu.menuInflater.inflate(R.menu.bank_recycler_edit_menu, popupEditMenu.menu)
                popupEditMenu.setOnMenuItemClickListener {
                    when ((it.itemId)) {
                        R.id.bankRecyclerEdit -> Toast.makeText(context, "1", Toast.LENGTH_SHORT).show()
                        else -> {
                            deleteEventData(dbHelper, arrayEventList[position])
                            updateRecyclerView(dbHelper, activity!!.applicationContext, view, date)
                            updateCalendar(calendar, dbHelper)
                        }
                    }
                    true
                }
                popupEditMenu.show()
            }
        })

        val cm = LinearLayoutManager(context)
        view.layoutManager = cm
        view.setHasFixedSize(true)
    }
}
