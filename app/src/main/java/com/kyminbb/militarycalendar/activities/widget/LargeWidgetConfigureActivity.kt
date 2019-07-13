package com.kyminbb.militarycalendar.activities.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.kyminbb.militarycalendar.R

/**
 * The configuration screen for the [LargeWidget] AppWidget.
 */
class LargeWidgetConfigureActivity : Activity() {
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    internal lateinit var mAppSeekBar: SeekBar
    internal lateinit var mAppTextView: TextView
    internal lateinit var widgetBackground: Drawable
    internal lateinit var mAppTestBackground: LinearLayout

    internal var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@LargeWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val opacityText = mAppTextView.text.toString()
        saveOpacityPref(context, mAppWidgetId, opacityText)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        LargeWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val largeResultValue = Intent()
        largeResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, largeResultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.large_widget_configure)
        val buttonOpacityTest = findViewById<View>(R.id.largeButtonTemp)
        mAppTextView = findViewById<View>(R.id.largeOpacityText) as TextView
        mAppSeekBar = findViewById<View>(R.id.largeSeekBar) as SeekBar
        mAppSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mAppTextView.text = p1.toString()
                widgetBackground = buttonOpacityTest.background
                widgetBackground.alpha = 255 - p1 * 255/100
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        buttonOpacityTest.setOnClickListener(mOnClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        mAppTextView.text = loadOpacityPref(this@LargeWidgetConfigureActivity, mAppWidgetId)
        mAppSeekBar.progress = mAppTextView.text.toString().toInt()
    }

    companion object {

        private val LARGE_PREFS_NAME = "com.kyminbb.militarycalendar.activities.widget.LargeWidget"
        private val LARGE_PREF_PREFIX_KEY = "LargeOpacity_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveOpacityPref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(LARGE_PREFS_NAME, 0).edit()
            prefs.putString(LARGE_PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadOpacityPref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(LARGE_PREFS_NAME, 0)
            val titleValue = prefs.getString(LARGE_PREF_PREFIX_KEY + appWidgetId, null)
            return titleValue ?: "0"
        }

        internal fun deleteOpacityPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(LARGE_PREFS_NAME, 0).edit()
            prefs.remove(LARGE_PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

