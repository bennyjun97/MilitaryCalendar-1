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
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    internal lateinit var mAppSeekBar: SeekBar
    internal lateinit var mAppTextView: TextView
    internal lateinit var widgetBackground: Drawable
    //internal lateinit var mAppBackgroundImageView: ImageView
    internal lateinit var mAppTestBackground: LinearLayout

    internal var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@SmallWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val opacityText = mAppTextView.text.toString()
        //val widgetText = mAppWidgetText.text.toString()
        saveOpacityPref(context, mAppWidgetId, opacityText)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        SmallWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.small_widget_configure)
        val buttonOpacityTest = findViewById<View>(R.id.buttonTemp)

        mAppTextView = findViewById<View>(R.id.opacityText) as TextView
        mAppTestBackground = findViewById<View>(R.id.transparentLayout) as LinearLayout
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            mAppTestBackground.setBackgroundResource(R.drawable.pseudoclock)
            //mAppTestBackground.setBackground(ContextCompat.getDrawable(this, FLAG_SYSTEM))
            //WallpaperManager.getInstance(this@SmallWidgetConfigureActivity).drawable
        }

        mAppSeekBar = findViewById<View>(R.id.seekBar) as SeekBar
        mAppSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mAppTextView.text = p1.toString()
                widgetBackground = buttonOpacityTest.background
                widgetBackground.alpha = 255 - p1 * 255/100
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        findViewById<View>(R.id.buttonTemp).setOnClickListener(mOnClickListener)



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

        //mAppWidgetText.setText(loadTitlePref(this@SmallWidgetConfigureActivity, mAppWidgetId))
        mAppTextView.text = loadOpacityPref(this@SmallWidgetConfigureActivity, mAppWidgetId)
        mAppSeekBar.progress = mAppTextView.text.toString().toInt()
    }

    companion object {

        private val PREFS_NAME = "com.kyminbb.militarycalendar.activities.widget.SmallWidget"
        private val PREF_PREFIX_KEY = "Opacitywidget_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveOpacityPref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadOpacityPref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return titleValue ?: "0"
        }

        internal fun deleteOpacityPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }

        internal fun getColorWithAlpha(color:Int, ratio: Float) : Int {
            return Color.argb(Math.round(Color.alpha(color) * ratio),
                Color.red(color), Color.green(color), Color.blue(color)
            )
        }
    }
}

