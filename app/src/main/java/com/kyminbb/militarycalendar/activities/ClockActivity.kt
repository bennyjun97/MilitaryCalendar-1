package com.kyminbb.militarycalendar.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kyminbb.militarycalendar.R
import kotlinx.android.synthetic.main.activity_clock.*

class ClockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        progressBar.max = 100
        progressBar.progress = 50
    }
}
