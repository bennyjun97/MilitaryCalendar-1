package com.kyminbb.militarycalendar.activities.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import kotlinx.android.synthetic.main.activity_graph.*

class GraphActivity : AppCompatActivity() {

    private var userInfo = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        loadData()

        button.setOnClickListener {

            Toast.makeText(this, userInfo.name, Toast.LENGTH_SHORT ).show()
            Toast.makeText(this, userInfo.affiliation, Toast.LENGTH_SHORT ).show()
            Toast.makeText(this, userInfo.promotionDates[Dates.ENLIST.ordinal].toString(), Toast.LENGTH_SHORT ).show()
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}
