package com.kyminbb.militarycalendar.activities.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.Display
import android.widget.RemoteViews
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.DateCalc
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import org.threeten.bp.LocalDateTime
import java.lang.Math.round
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class MiltaryWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        private var userInfo = User();

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            // load data
            val prefs = context.getSharedPreferences("prefs", MODE_PRIVATE)
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

            // show promotion, name, percent, d-day이름, 전역일, 퍼센트
            val promotionText =
                when(userInfo.rank) {
                    0 -> "이등병"
                    1 -> "일병"
                    2 -> "상병"
                    3 -> "병장"
                    else -> ""
                }
            val nameText = userInfo.name
            val percentText =
                (kotlin.math.round(DateCalc.entirePercent(enlistDateTime, etsDateTime)*10)/10.0).toString()+"%"
            val dDayText = DateCalc.countDDay(etsDateTime)
            val numVacationText = "77일"
            //val widgetText = context.getString(R.string.appwidget_text)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.miltary_widget)
            views.setTextViewText(R.id.promotion, promotionText)
            views.setTextViewText(R.id.name, nameText)
            views.setTextViewText(R.id.percent, percentText)
            views.setTextViewText(R.id.dDay, dDayText)
            views.setImageViewResource(R.id.promotionImage, R.drawable.army4)
            views.setTextViewText(R.id.numVacationDays, numVacationText)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

