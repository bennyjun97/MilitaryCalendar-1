package com.kyminbb.militarycalendar.activities.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.activities.main.HomeActivity
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import org.threeten.bp.LocalDateTime

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [LargeWidgetConfigureActivity]
 */
class LargeWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            LargeWidgetConfigureActivity.deleteOpacityPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val views = RemoteViews(context!!.packageName, R.layout.large_widget)

        /* swap views when next button is clicked */
        if(ACTION_UPDATE_CLICK_NEXT.equals(intent!!.action)){
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val largeWidget = ComponentName(context, LargeWidget::class.java)

            when (layoutNum%3){
                0 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.GONE)
                    views.setViewVisibility(R.id.secondLayout1, View.VISIBLE)
                    views.setViewVisibility(R.id.secondLayout2, View.GONE)
                }
                1 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.GONE)
                    views.setViewVisibility(R.id.secondLayout1, View.GONE)
                    views.setViewVisibility(R.id.secondLayout2, View.VISIBLE)
                }
                2 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.VISIBLE)
                    views.setViewVisibility(R.id.secondLayout1, View.GONE)
                    views.setViewVisibility(R.id.secondLayout2, View.GONE)
                }
            }
            layoutNum = (layoutNum + 1) % 3
            appWidgetManager.updateAppWidget(largeWidget, views)
        }
        /* swap views when back button is clicked */
        else if(ACTION_UPDATE_CLICK_BACK.equals(intent.action)){
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val largeWidget = ComponentName(context, LargeWidget::class.java)
            when (layoutNum%3){
                0 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.GONE)
                    views.setViewVisibility(R.id.secondLayout1, View.GONE)
                    views.setViewVisibility(R.id.secondLayout2, View.VISIBLE)
                }
                1 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.VISIBLE)
                    views.setViewVisibility(R.id.secondLayout1, View.GONE)
                    views.setViewVisibility(R.id.secondLayout2, View.GONE)
                }
                2 -> {
                    views.setViewVisibility(R.id.secondLayout0, View.GONE)
                    views.setViewVisibility(R.id.secondLayout1, View.VISIBLE)
                    views.setViewVisibility(R.id.secondLayout2, View.GONE)
                }
            }
            layoutNum = (layoutNum + 2) % 3
            appWidgetManager.updateAppWidget(largeWidget, views)
        }

    }



    companion object {

        private var userInfo = User()

        private const val ACTION_UPDATE_CLICK_NEXT = "action.UPDATE_CLICK_NEXT"
        private const val ACTION_UPDATE_CLICK_BACK = "action.UPDATE_CLICK_BACK"

        private var layoutNum = 0


        // get intent for swapping views
        private fun getPendingSelfIntent(context: Context, action: String) : PendingIntent {
            val intent = Intent(context, LargeWidget::class.java)
            intent.action = action
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            /** load Data **/
            // load userInfo data
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)

            // load LocalDate data
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

            // load promotion, name, D-day, percent, "남은 휴가", numVacationDays data
            val views = RemoteViews(context.packageName, R.layout.large_widget)
            val promotionText = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
            val hobongText = DateCalc.calcMonth(userInfo)
            val percentTotal =
                kotlin.math.round(DateCalc.entirePercent(enlistDateTime, etsDateTime)*10)/10.0
            val percentPromotion =
                kotlin.math.round(DateCalc.rankPercent(userInfo)*10)/10.0
            val percentHobong =
                kotlin.math.round(DateCalc.monthPercent(userInfo)*10)/10.0
            val dDayText = DateCalc.countDDay(etsDateTime)
            //val progress = 100 - actual progress(to be implemented)
            val vacationText = context.getText(R.string.Vacation_text).toString()
            val numVacationText = context.getText(R.string.numVacationDay_text).toString()
            val nextVacation = context.getText(R.string.nextVacation).toString()
            val nextVacationDate = context.getText(R.string.nextVacation_Date).toString()
            val dDayVacation = context.getText(R.string.dday_text).toString()


            // load opacity (opacity is hexadecimal) data
            val alphaNum = (100 - LargeWidgetConfigureActivity.
                loadOpacityPref(context, appWidgetId).toInt())*255/100
            val alpha =
                when (alphaNum) {
                    in 0..15 -> "0" + Integer.toHexString(alphaNum).toUpperCase()
                    else -> Integer.toHexString(alphaNum).toUpperCase()
                }

            /** Instantiate the views**/
            // set progressbar indicater text
            views.setTextViewText(R.id.largeProgressTextSecond, "현재 ${promotionText}")
            views.setTextViewText(R.id.largeProgressTextThird, "현재 ${hobongText}호봉")

            // set progressbar
            views.setProgressBar(
                R.id.progress_horizontal_first, 1000, (percentTotal*10).toInt(), false)
            views.setProgressBar(
                R.id.progress_horizontal_second, 1000, (percentPromotion*10).toInt(), false)
            views.setProgressBar(
                R.id.progress_horizontal_third, 1000, (percentHobong*10).toInt(), false)

            // set progressbar percent text
            views.setTextViewText(R.id.largePercentFirst, "${percentTotal}%")
            views.setTextViewText(R.id.largePercentSecond, "${percentPromotion}%")
            views.setTextViewText(R.id.largePercentThird, "${percentHobong}%")

            // set D-day
            views.setTextViewText(R.id.largeDDay, dDayText)

            // set vacation progress bar (100 - actual progress(to be implemented) because it is counter-clockwise)
            views.setProgressBar(R.id.progress_circular, 100, 75, false)

            // set text for memos, notification
            views.setTextViewText(R.id.large_vacation, vacationText)
            views.setTextViewText(R.id.large_numVacation, numVacationText)
            views.setTextViewText(R.id.largeNextVacation, nextVacation)
            views.setTextViewText(R.id.largeNextVacationDate, nextVacationDate)
            views.setTextViewText(R.id.largeDDayVacation, dDayVacation)

            // set button image resource
            views.setImageViewResource(R.id.swapNextButton, R.drawable.right_white)
            views.setImageViewResource(R.id.swapBackButton, R.drawable.left_white)

            // set buttonListener for swapping views
            views.setOnClickPendingIntent(R.id.swapNextButton,
                getPendingSelfIntent(context, ACTION_UPDATE_CLICK_NEXT))
            views.setOnClickPendingIntent(R.id.swapBackButton,
                getPendingSelfIntent(context, ACTION_UPDATE_CLICK_BACK))
            // set Opacity with alpha values(loaded above)
            views.setInt(R.id.largeWidgetLayout, "setBackgroundColor",
                Color.parseColor("#${alpha}333333"))

            /** Instantiate Intents **/
            // Widgets can only receive PendingIntent class as intents
            // main intent (goes to HomeActivity) when the widget is onClicked
            val largeMainIntent:PendingIntent = Intent(context, HomeActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            // configure intent (goes to configure activity) when setting button is onClicked
            val largeConfigureIntent: PendingIntent = Intent(context, LargeWidgetConfigureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

            // apply the intents to the widget view
            views.apply{setOnClickPendingIntent(R.id.largeWidgetLayout, largeMainIntent)}
                .apply{setOnClickPendingIntent(R.id.largeConfigureButton, largeConfigureIntent)}

            /** Instruct Widget Manager**/
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

