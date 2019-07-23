package com.kyminbb.militarycalendar.activities.main


import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.Bank
import com.kyminbb.militarycalendar.utils.BankRvAdapter
import com.kyminbb.militarycalendar.utils.Dates
import com.kyminbb.militarycalendar.utils.User
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_deposit.*
import org.jetbrains.anko.find
import org.threeten.bp.LocalDate
import java.lang.Double.parseDouble
import java.text.DecimalFormat


/**
 * A simple [Fragment] subclass.
 *
 */
class DepositFragment : Fragment() {


    private var userInfo = User()
    private val today = LocalDate.now()

    private val decimalFormat = DecimalFormat("#,###")
    private var temp = ""
    private var bankList = arrayListOf<Bank>(
        Bank("신한은행", "1,000,000원", "월 200,000원"),
        Bank("우리은행", "800,000원", "월 100,000원"),
        Bank("광주은행", "200,000원", "월 50,000원")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deposit, container, false)
    }

    /**
     * onViewCreated is divied into 4 parts of code -> to be restructured later.
     * 1. Implementing Montly Deposit(적금) infos
     * 2. Implementing Montly Graph
     * 3. Implementing current well-being(적금이 잘 이루어지는지에 대한 평가)
     * 4. Implementing addButton on the top-right corner
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()

        /** Implement Montly Deposit Info using Recycler View */
        // add recylcerView
        val adapter = BankRvAdapter(activity!!.applicationContext, bankList)
        bankRecyclerView.adapter = adapter

        // add layoutManager
        val lm = LinearLayoutManager(activity!!.applicationContext)
        bankRecyclerView.layoutManager = lm
        bankRecyclerView.setHasFixedSize(true)


        /** Implement addButton on the top-right corner */
        // Show pop-up when add button is clicked
        val popupView = layoutInflater.inflate(R.layout.add_deposit, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true

        // Add new deposit information
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
            //val bankNameSpinner = popupView.find<Spinner>(R.id.spinner)


            /* Add functionality to buttons */
            // set end date default as the 전역일(end date)
            val localDateEnd = userInfo.promotionDates[Dates.END.ordinal]
            bankEndDateButton.text = date2String(
                localDateEnd.year, localDateEnd.monthValue, localDateEnd.dayOfMonth
            )
            val bankArr = resources.getStringArray(R.array.bank_string)
            val bankArrButtons = arrayOf(
                R.id.bankSuhyeob, R.id.bankShinhan, R.id.bankWoori, R.id.bankHana,
                R.id.bankKookmin, R.id.bankNonghyeob, R.id.bankIndustrial, R.id.bankDaegu,
                R.id.bankBusan, R.id.bankGyeongnam, R.id.bankGwangju,
                R.id.bankJeonbook, R.id.bankJeju, R.id.bankPostOffice
            )
            // input information
            bankNameButton.setOnClickListener {
                val popupMenu = PopupMenu(activity, bankNameButton)
                popupMenu.menuInflater.inflate(R.menu.bank_name_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener {
                    bankNameButton.text = bankArr[bankArrButtons.indexOf(it.itemId)]
                    true
                }
                popupMenu.show()
            }
            // input the date from spinnerDatePicker, default: today
            bankStartDateButton.setOnClickListener { setDate(bankStartDateButton) }
            // input the endDate from spinnerDatePicker, default: userInfo.promotion.endDate
            bankEndDateButton.setOnClickListener { setDate(bankEndDateButton) }
            // input the deposit amount, using another pop-up view
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
                                parseDouble(
                                    charSequence.toString().replace(",".toRegex(), "")
                                )
                            )
                            depositAmountEditText.setText(temp)
                            depositAmountEditText.setSelection(temp.length)
                        }
                    }

                    override fun afterTextChanged(editable: Editable) {}
                }
                depositAmountEditText.addTextChangedListener(watcher)

                /* Add functionality to buttons */
                // cancel input
                depositPopUpCancel.setOnClickListener { popupDeposit.dismiss() }
                // save input
                depositPopUpSave.setOnClickListener {
                    bankDepositAmountButton.text = "${depositAmountEditText.text}원"
                    popupDeposit.dismiss()
                }
            }
            bankInterestButton.setOnClickListener {}

            /* make decision on init, cancel, registering bank information */
            // create an arrayList of info-buttons for convenience
            val buttonArr = arrayOf(
                bankNameButton, bankStartDateButton, bankEndDateButton,
                bankDepositAmountButton, bankInterestButton
            )
            // init
            bankInitButton.setOnClickListener {
                for (infoButton in buttonArr)
                    infoButton.text = ""
            }
            // cancel
            bankCancelButton.setOnClickListener {
                for (infoButton in buttonArr)
                    infoButton.text = ""
                popup.dismiss()
            }
            // register
            bankRegisterButton.setOnClickListener { /* save */ }
        }
    }

    // set datePicker functionality for buttons
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


    // convert date into format that are SQLite readable
    private fun date2String(year: Int, month: Int, dayOfMonth: Int): String {
        return "$year-$month-$dayOfMonth"
    }

    // load data
    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }
}
