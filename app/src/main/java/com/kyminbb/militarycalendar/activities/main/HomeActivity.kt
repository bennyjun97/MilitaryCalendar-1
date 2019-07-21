package com.kyminbb.militarycalendar.activities.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    // ViewPagerAdapter in .utils
    private val adapter = ViewPagerAdapter(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        createTabs()
    }

    private fun createTabs() {
        adapter.addFragment(CalendarFragment(), "짬중일지")
        adapter.addFragment(ClockFragment(), "짬중현황")
        adapter.addFragment(GraphFragment(), "짬중성취")



        // Link fragments and the activity.
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        val tabIcons = arrayOf(
            R.drawable.calendar,
            R.drawable.clock,
            R.drawable.graph
        )
        tabs.getTabAt(0)!!.setIcon(tabIcons[0])
        tabs.getTabAt(1)!!.setIcon(tabIcons[1])
        tabs.getTabAt(2)!!.setIcon(tabIcons[2])

    }
}
