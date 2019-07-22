package com.kyminbb.militarycalendar.activities.main


import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.commit451.addendum.threetenabp.toLocalDate
import com.google.gson.Gson

import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.add_deposit.*
import kotlinx.android.synthetic.main.fragment_calendar2.*
import kotlinx.android.synthetic.main.fragment_deposit.*
import org.jetbrains.anko.find
import org.threeten.bp.LocalDate
import java.text.DecimalFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 *
 */
class DepositFragment : Fragment() {

    private var userInfo = User()
    private val today = LocalDate.now()
    private val decimalFormat = DecimalFormat("#,###")
    private var temp = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deposit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        /**
         * when the top-right button is pressed,
         * the pop-up menu appears to enroll a new monthly deposit
         */
        // Show pop-up when add button is clicked
        val popupView = layoutInflater.inflate(R.layout.add_deposit, null)
        val popup = PopupWindow(popupView)
        // enables editText


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
            val bankInitButton = popupView.find<Button>(R.id.bankInitButton)
            val bankCancelButton = popupView.find<Button>(R.id.bankCancelButton)
            val bankRegisterButton = popupView.find<Button>(R.id.bankRegisterButton)

            // add functionality to each buttons
            // set end date default as the 전역일(end date)
            val localDateEnd = userInfo.promotionDates[Dates.END.ordinal]
            bankEndDateButton.text = date2String(
                localDateEnd.year, localDateEnd.monthValue, localDateEnd.dayOfMonth
            )
            // input information
            bankNameButton.setOnClickListener { }

            bankStartDateButton.setOnClickListener {
                // input the date from spinnerDatePicker, default : today
                setDate(bankStartDateButton)
            }

            bankEndDateButton.setOnClickListener {
                // the enddate default is the endDATe
                setDate(bankEndDateButton)
            }
            bankDepositAmountButton.setOnClickListener {
                // enable deposit Popup
                val popupDepositView = layoutInflater.inflate(R.layout.add_deposit_amount, null)
                val popupDeposit = PopupWindow(popupDepositView)
                popupDeposit.isFocusable = true
                popupDeposit.showAtLocation(view, Gravity.CENTER, 0, 0)
                popupDeposit.update(
                    view,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels
                )

                // enable buttons, editTexts
                val depositPopUpCancel = popupDepositView.find<Button>(R.id.depositPopUpCancel)
                val depositPopUpSave = popupDepositView.find<Button>(R.id.depositPopUpSave)
                val depositAmountEditText = popupDepositView.find<EditText>(R.id.depositAmountEditText)


                // format editText into Korean Currency
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        if (!TextUtils.isEmpty(charSequence.toString()) && charSequence.toString() != temp) {
                            temp = decimalFormat.format(
                                java.lang.Double.parseDouble(
                                    charSequence.toString().replace(
                                        ",".toRegex(),
                                        ""
                                    )))
                            depositAmountEditText.setText(temp)
                            depositAmountEditText.setSelection(temp.length)
                        }
                    }
                    override fun afterTextChanged(editable: Editable) {
                    }
                }
                depositAmountEditText.addTextChangedListener(watcher)

                // add onClickListener to buttons
                depositPopUpCancel.setOnClickListener{popupDeposit.dismiss()}
                depositPopUpSave.setOnClickListener{
                    bankDepositAmountButton.text = "${depositAmountEditText.text}원"
                    popupDeposit.dismiss()
                }
            }
/*
            bankDepositAmountButton.setOnClickListener {
                var temp = ""
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        if (!TextUtils.isEmpty(charSequence.toString()) && charSequence.toString() != temp) {
                            temp = decimalFormat.format(
                                java.lang.Double.parseDouble(
                                    charSequence.toString().replace(
                                        ",".toRegex(),
                                        ""
                                    )))

                        }
                    }
                    override fun afterTextChanged(editable: Editable) {
                    }
                }
            }
*/
            bankInterestButton.setOnClickListener {}

            // make decision on init, cancel, registering bank infos
            bankInitButton.setOnClickListener { /* init */ }
            bankCancelButton.setOnClickListener { popup.dismiss() }
            bankRegisterButton.setOnClickListener { /* save */ }
        }
    }

    // To be revised to display the selected date for the default.
    private fun setDate(button: Button) {
        // Use SpinnerDatePicker to select date.
        // https://github.com/drawers/SpinnerDatePicker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                button.text = date2String(year, month + 1, day)
        }
        val dialog = SpinnerDatePickerDialogBuilder()
            .context(context)
            .callback(dateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
            .maxDate(today.year + 4, 11, 31)
            .minDate(today.year - 5, 0, 1)
            .defaultDate(today.year, today.monthValue - 1, today.dayOfMonth)
        dialog.build().show()
    }

    private fun date2String(year: Int, month: Int, dayOfMonth: Int): String {
        return "$year-$month-$dayOfMonth"
    }

    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}
