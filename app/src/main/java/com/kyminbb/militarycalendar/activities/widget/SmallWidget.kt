package com.kyminbb.militarycalendar.activities.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
 * App Widget Configuration implemented in [SmallWidgetConfigureActivity]
 */
class SmallWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            SmallWidgetConfigureActivity.deleteOpacityPref(context, appWidgetId)
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
            val percentText =
                (kotlin.math.round(DateCalc.entirePercent(enlistDateTime, etsDateTime)*10)/10.0).toString()+"%"
            val dDayText = DateCalc.countDDay(etsDateTime)
            //val numVacationText = "77일"
            val numVacationText = SmallWidgetConfigureActivity.loadOpacityPref(context, appWidgetId)


            val alphaNum = (100 - SmallWidgetConfigureActivity.
                loadOpacityPref(context, appWidgetId).toInt())*255/100
            val alpha =
                when (alphaNum) {
                    in 0..15 -> "0" + Integer.toHexString(alphaNum).toUpperCase()
                    else -> Integer.toHexString(alphaNum).toUpperCase()
                }

            // Construct the RemoteViews object, and instantiate the views using Remoteviews
            val views = RemoteViews(context.packageName, R.layout.small_widget)
            views.setImageViewResource(R.id.smallPromotionImage, promotionImageDefault + userInfo.rank)
            views.setTextViewText(R.id.smallPromotion, promotionText)
            views.setTextViewText(R.id.smallName, nameText)
            views.setTextViewText(R.id.smallDDay, dDayText)
            views.setTextViewText(R.id.smallPercent, percentText)
            views.setTextViewText(R.id.smallNumVacationDays, numVacationText)
            views.setInt(R.id.smallWidgetLayout, "setBackgroundColor",
                Color.parseColor("#${alpha}424242"))


            /**
             * Construct Intents so that when the widget is clicked, it moves to calendarfragment
             * when the setting button is clicked, it moves to widget configuration
             */
            // mainIntent to move to ClockFragment
            val smallMainIntent: PendingIntent = Intent(context, HomeActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            // configureIntent to move to Configuration
            val smallConfigureIntent: PendingIntent = Intent(context, SmallWidgetConfigureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

            // apply Intent
            views.apply{setOnClickPendingIntent(R.id.smallWidgetLayout, smallMainIntent)}
                .apply{setOnClickPendingIntent(R.id.smallConfigureButton, smallConfigureIntent)}

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

