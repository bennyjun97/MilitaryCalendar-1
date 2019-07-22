package com.kyminbb.militarycalendar.activities.main


import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow

import com.kyminbb.militarycalendar.R
import kotlinx.android.synthetic.main.add_deposit.*
import kotlinx.android.synthetic.main.fragment_calendar2.*
import kotlinx.android.synthetic.main.fragment_deposit.*
import org.jetbrains.anko.find

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 *
 */
class DepositFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deposit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        /**
         * when the top-right button is pressed,
         * the pop-up menu appears to enroll a new monthly deposit
         */
        // Show pop-up when add button is clicked
        val popupView = layoutInflater.inflate(R.layout.add_deposit, null)
        val popup = PopupWindow(popupView)


        depositButtonAdd.setOnClickListener {
            popup.showAtLocation(view, Gravity.CENTER, 0, 0)
            popup.update(
                view,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )

            // enable buttons as views
            val bankNameButton = popupView.find<Button>(R.id.bankNameButton)
            val bankStartDateButton = popupView.find<Button>(R.id.bankStartDateButton)
            val bankEndDateButton = popupView.find<Button>(R.id.bankEndDateButton)
            val bankDepositAmountButton = popupView.find<Button>(R.id.bankDepositAmountButton)
            val bankInterestButton = popupView.find<Button>(R.id.bankInterestButton)

            bankNameButton.setOnClickListener { }
            bankStartDateButton.setOnClickListener { }
            bankEndDateButton.setOnClickListener { }
            bankDepositAmountButton.setOnClickListener { }
            bankInterestButton.setOnClickListener {}


            // make decision on init, cancel, registering bank infos
            bankInitButton.setOnClickListener { /* init */ }
            bankCancelButton.setOnClickListener { popup.dismiss() }
            bankRegisterButton.setOnClickListener { /* save */ }
        }
    }
}
