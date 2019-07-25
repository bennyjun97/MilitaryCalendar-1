package com.kyminbb.militarycalendar.activities.widget

import android.app.Activity
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_SYSTEM
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.kyminbb.militarycalendar.R
import org.jetbrains.anko.view

/**
 * The configuration screen for the [SmallWidget] AppWidget.
 */
class SmallWidgetConfigureActivity : Activity() {

    /** instantiate properties **/
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var mAppSeekBar: SeekBar
    private lateinit var mAppTextView: TextView
    private lateinit var widgetBackground: Drawable
    private lateinit var mAppTestBackground: LinearLayout
    private var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@SmallWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val opacityText = mAppTextView.text.toString()
        saveOpacityPref(context, mAppWidgetId, opacityText)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        SmallWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val smallResultValue = Intent()
        smallResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, smallResultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        // set content view for configuration screen
        setContentView(R.layout.small_widget_configure)
        val buttonOpacityTest = findViewById<View>(R.id.smallButtonTemp)
        buttonOpacityTest.setOnClickListener(mOnClickListener)
        mAppTextView = findViewById<View>(R.id.smallOpacityText) as TextView
        mAppTextView.text = loadOpacityPref(this@SmallWidgetConfigureActivity, mAppWidgetId)
        mAppTestBackground = findViewById<View>(R.id.smallTransparentLayout) as LinearLayout
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            mAppTestBackground.setBackgroundResource(R.drawable.cloud)
            //mAppTestBackground.setBackground(ContextCompat.getDrawable(this, FLAG_SYSTEM))
            //WallpaperManager.getInstance(this@SmallWidgetConfigureActivity).drawable
        }
        mAppSeekBar = findViewById<View>(R.id.smallSeekBar) as SeekBar
        mAppSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mAppTextView.text = p1.toString()
                widgetBackground = buttonOpacityTest.background
                widgetBackground.alpha = 255 - p1 * 255/100
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        mAppSeekBar.progress = mAppTextView.text.toString().toInt()

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
    }

    /** manage data (save, load, delete) **/
    companion object {

        private const val SMALL_PREFS_NAME = "com.kyminbb.militarycalendar.activities.widget.SmallWidget"
        private const val SMALL_PREF_PREFIX_KEY = "SmallOpacity_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveOpacityPref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(SMALL_PREFS_NAME, 0).edit()
            prefs.putString(SMALL_PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadOpacityPref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(SMALL_PREFS_NAME, 0)
            val titleValue = prefs.getString(SMALL_PREF_PREFIX_KEY + appWidgetId, null)
            return titleValue ?: "0"
        }

        // Delete opacity data when the widget is removed
        internal fun deleteOpacityPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(SMALL_PREFS_NAME, 0).edit()
            prefs.remove(SMALL_PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

