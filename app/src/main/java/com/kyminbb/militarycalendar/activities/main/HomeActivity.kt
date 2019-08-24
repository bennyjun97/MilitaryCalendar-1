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
        adapter.addFragment(ClockFragment(), "짬중현황")
        adapter.addFragment(CalendarFragment2(), "짬중일지")
        adapter.addFragment(DepositFragment(), "짬중화폐")

        // Link fragments and the activity.
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        val tabIcons = arrayOf(
            R.drawable.clock,
            R.drawable.calendar,
            R.drawable.deposit
        )
        tabs.getTabAt(0)!!.setIcon(tabIcons[0])
        tabs.getTabAt(1)!!.setIcon(tabIcons[1])
        tabs.getTabAt(2)!!.setIcon(tabIcons[2])


        if(viewPager.currentItem == 0){
            isFirstPage = true
        }

    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        var isFirstPage = false
    }
}
