package com.kyminbb.militarycalendar.activities.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.TextView
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

    companion object {

        private var userInfo = User()

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

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

            // calculate promotion, name, D-day, percent, "남은 휴가", numVacationDays
            val promotionImageDefault = R.drawable.rank1
            val promotionText = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
            val nameText = userInfo.name
            val percentText = context.getText(R.string.large_percent_text).toString()
            val percent =
                (kotlin.math.round(DateCalc.entirePercent(enlistDateTime, etsDateTime)*10)/10.0).toString()+"%"
            val untilNextText = context.getText(R.string.large_until_text).toString()
            val dDayText = DateCalc.countDDay(etsDateTime)
            //val progress = 100 - actual progress(to be implemented)
            val vacationText = context.getText(R.string.Vacation_text).toString()
            val numVacationText = context.getText(R.string.numVacationDay_text).toString()
            val nextVacation = context.getText(R.string.nextVacation).toString()
            val nextVacationDate = context.getText(R.string.nextVacation_Date).toString()
            val dDayVacation = context.getText(R.string.dday_text).toString()


            val alphaNum = (100 - LargeWidgetConfigureActivity.
                loadOpacityPref(context, appWidgetId).toInt())*255/100
            val alpha =
                when (alphaNum) {
                    in 0..15 -> "0" + Integer.toHexString(alphaNum).toUpperCase()
                    else -> Integer.toHexString(alphaNum).toUpperCase()
                }

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.large_widget)
            views.setImageViewResource(R.id.largePromotionImage, promotionImageDefault + userInfo.rank)
            views.setTextViewText(R.id.largePromotion, promotionText)
            views.setTextViewText(R.id.largeName, nameText)
            views.setTextViewText(R.id.largePercent_text, percentText)
            views.setTextViewText(R.id.largePercent, percent)
            views.setTextViewText(R.id.largeUntil_next, untilNextText)
            views.setTextViewText(R.id.largeDDay, dDayText)
            views.setProgressBar(R.id.progress_circular, 100, 75, false)
            views.setTextViewText(R.id.large_vacation, vacationText)
            views.setTextViewText(R.id.large_numVacation, numVacationText)
            views.setTextViewText(R.id.largeNextVacation, nextVacation)
            views.setTextViewText(R.id.largeNextVacationDate, nextVacationDate)
            views.setTextViewText(R.id.largeDDayVacation, dDayVacation)
            views.setInt(R.id.largeWidgetLayout, "setBackgroundColor",
                Color.parseColor("#${alpha}313A31"))


            // Construct the intents
            // main intent (goes to homeactivity)
            val largeMainIntent:PendingIntent = Intent(context, HomeActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            // configure intent (goes to configure activity)
            val largeConfigureIntent: PendingIntent = Intent(context, LargeWidgetConfigureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            // apply the intents to the widget view
            views.apply{setOnClickPendingIntent(R.id.largeWidgetLayout, largeMainIntent)}
                .apply{setOnClickPendingIntent(R.id.largeConfigureButton, largeConfigureIntent)}

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

