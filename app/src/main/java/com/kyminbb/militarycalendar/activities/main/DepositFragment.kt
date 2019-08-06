package com.kyminbb.militarycalendar.activities.main


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kyminbb.militarycalendar.R
import com.kyminbb.militarycalendar.utils.*
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.add_deposit.*
import kotlinx.android.synthetic.main.add_interest.view.*
import kotlinx.android.synthetic.main.fragment_deposit.*
import org.jetbrains.anko.*
import org.threeten.bp.LocalDate
import org.w3c.dom.Text
import java.lang.Double.parseDouble
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.roundToInt

class DepositFragment : Fragment() {

    companion object {
        private const val animStyle = R.style.Animation_AppCompat_DropDownUp
        private const val BANK_PREFS_NAME = "com.kyminbb.militarycalendar.activities.main.DepositFragment"
        private const val BANK_PREF_PREFIX_KEY = "BANK_NAME_"
        private const val ADD_BUTTON_INFO = 99
    }

    private var userInfo = User()
    private val today = LocalDate.now()
    private val decimalFormat = DecimalFormat("#,###")
    private var temp = ""
    private var bankIndex = 0
    private var monthlySum = 0
    private var bankTotalSum = 0
    //private var arrayBankList = loadBankData(this.context!!)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deposit, container, false)
    }

    /**
     * onViewCreated is divided into 4 parts of code -> to be restructured later.
     * 1. Implementing Monthly Deposit(적금) information
     * 2. Implementing Predicted Earnings
     * 3. Implementing current well-being(적금이 잘 이루어지는지에 대한 평가)
     * 4. Implementing addButton on the top-right corner
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()

        /** Implement Monthly Deposit Info using Recycler View */
        loadBankData(activity!!.applicationContext)
        updateRecyclerView(activity!!.applicationContext, bankRecyclerView)

        /** Implement Predicted Earnings **/
        /** Implement Current Well-Being **/
        updateScoreView()

        /** Implement addButton on the top-right corner */
        // Add new deposit information
        depositButtonAdd.setOnClickListener {
            showBigPopUp(ADD_BUTTON_INFO)
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
        return "$year-${"%02d".format(month)}-${"%02d".format(dayOfMonth)}"
    }

    // load userInfo data
    private fun loadData() {
        // Get context from the parent activity.
        val prefs = this.context!!.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        userInfo = Gson().fromJson(prefs.getString("userInfo", ""), User::class.java)
    }

    // show big pop-up for adding/editing bank information
    private fun showBigPopUp(position: Int) {
        // Show pop-up when add button is clicked
        val popupView = layoutInflater.inflate(R.layout.add_deposit, null)
        val popup = PopupWindow(popupView)
        popup.isFocusable = true
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        popup.animationStyle =animStyle
        popup.update(
            view,
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels
        )

        // enable buttons as views
        val popupTitle = popupView.find<TextView>(R.id.popUpBankTitle)
        val bankNameButton = popupView.find<Button>(R.id.bankNameButton)
        val bankStartDateButton = popupView.find<Button>(R.id.bankStartDateButton)
        val bankEndDateButton = popupView.find<Button>(R.id.bankEndDateButton)
        val bankDepositAmountButton = popupView.find<Button>(R.id.bankDepositAmountButton)
        val bankInterestButton = popupView.find<Button>(R.id.bankInterestButton)
        val bankInitButton = popupView.find<Button>(R.id.bankInitButton)
        val bankCancelButton = popupView.find<Button>(R.id.bankCancelButton)
        val bankRegisterButton = popupView.find<Button>(R.id.bankRegisterButton)


        /* if mode is unique, then just show the end date as 전역일
        *  if mode is edit, then show the data stored */
        if (position in 0 until 14) {
            popupTitle.text = "은행 정보 수정하기"
            val bankInfo = loadBankData(this.context!!)[position]
            bankNameButton.text = bankInfo.bankName
            bankStartDateButton.text = DateCalc.localDateToString(bankInfo.startDate)
            bankEndDateButton.text = DateCalc.localDateToString(bankInfo.endDate)
            bankDepositAmountButton.text = "${decimalFormat.format(bankInfo.monthDeposit)}원"
            bankInterestButton.text = "${bankInfo.interest}%"
        }

        /* Add functionality to buttons */
        // set end date default as the 전역일(end date)
        bankEndDateButton.text = DateCalc.localDateToString(userInfo.promotionDates[Dates.END.ordinal])
        val bankArr = resources.getStringArray(R.array.bank_string)
        val bankArrButtons = arrayOf(
            R.id.bankSuhyeob, R.id.bankShinhan, R.id.bankWoori, R.id.bankHana,
            R.id.bankKookmin, R.id.bankNonghyeob, R.id.bankIndustrial, R.id.bankDaegu,
            R.id.bankBusan, R.id.bankGyeongnam, R.id.bankGwangju,
            R.id.bankJeonbook, R.id.bankJeju, R.id.bankPostOffice
        )

        // input information
        // bank name button is only activated when addButton is clicked
        if (position == ADD_BUTTON_INFO){
            bankNameButton.setOnClickListener {
                val popupMenu = PopupMenu(activity, bankNameButton)
                popupMenu.menuInflater.inflate(R.menu.bank_name_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener {
                    // initialize bankIndex -> to be used for interest information popup
                    bankIndex = bankArrButtons.indexOf(it.itemId)
                    bankNameButton.text = bankArr[bankIndex]
                    true
                }
                popupMenu.show()
            }
        } else {
            bankIndex = bankArr.indexOf(bankNameButton.text)
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
            popupDeposit.animationStyle = animStyle
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
                    if (!isEmpty(charSequence.toString()) && charSequence.toString() != temp) {
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

            // cancel input
            depositPopUpCancel.setOnClickListener { popupDeposit.dismiss() }
            // save input
            depositPopUpSave.setOnClickListener {
                if(!isEmpty(depositAmountEditText.text)) {
                    bankDepositAmountButton.text = "${depositAmountEditText.text}원"
                }
                popupDeposit.dismiss()
            }
        }

        // 이자율 입력!!
        bankInterestButton.setOnClickListener {
            val popupDepositView = layoutInflater.inflate(R.layout.add_interest, null)
            val popupDeposit = PopupWindow(popupDepositView)
            popupDeposit.animationStyle = animStyle
            popupDeposit.isFocusable = true

            popupDeposit.showAtLocation(view, Gravity.CENTER, 0, 0)
            popupDeposit.update(
                view,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
            // enable buttons, editTexts
            val interestPopUpInfo = popupDepositView.find<ImageButton>(R.id.interestPopUpInfo)
            val interestPopUpCancel = popupDepositView.find<Button>(R.id.interestPopUpCancel)
            val interestPopUpSave = popupDepositView.find<Button>(R.id.interestPopUpSave)
            val depositInterestEditText = popupDepositView.find<EditText>(R.id.depositInterestEditText)

            // show info (은행 이자율 정보)
            interestPopUpInfo.setOnClickListener{
                val popupInterestInfo = layoutInflater.inflate(R.layout. interest_info, null)
                val popupInfo = PopupWindow(popupInterestInfo)
                popupInfo.animationStyle = animStyle
                popupInfo.isFocusable = true

                popupInfo.showAtLocation(view, Gravity.CENTER, 0, 0)
                popupInfo.update(
                    view,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels
                )

                val infoExit = popupInterestInfo.find<ImageButton>(R.id.infoExitButton)

                // initialize items(1~20) as popupView for infoPopUp
                val itemArrayList : ArrayList<TextView> = arrayListOf()
                for (item in 0 until 20) { itemArrayList.add(popupInterestInfo.find(R.id.item01 + item)) }
                // get text array that matches bankIndex, save text
                val textArray : Array<String> = resources.getStringArray(R.array.infoArray01 + bankIndex)
                for(i in 0 until textArray.size ){ itemArrayList[i].text = textArray[i] }

                infoExit.setOnClickListener{
                    popupInfo.dismiss()
                }


            }
            // cancel input
            interestPopUpCancel.setOnClickListener { popupDeposit.dismiss() }
            // save input
            interestPopUpSave.setOnClickListener {
                if (!isEmpty(depositInterestEditText.text)) {
                    bankInterestButton.text = "${depositInterestEditText.text}%"
                }
                popupDeposit.dismiss()
            }
        }


        /* make decision on init, cancel, registering bank information */
        // create an arrayList of info-buttons for convenience
        val buttonArr = arrayOf(
            bankNameButton, bankStartDateButton, bankEndDateButton,
            bankDepositAmountButton, bankInterestButton
        )
        // init
        bankInitButton.setOnClickListener {
            when(position) {
                ADD_BUTTON_INFO ->
                    for (infoButton in buttonArr)
                        infoButton.text = ""
                else -> {
                    val temp = bankNameButton.text
                    for (infoButton in buttonArr)
                        infoButton.text = ""
                    bankNameButton.text = temp
                }
            }
        }
        // cancel
        bankCancelButton.setOnClickListener {
            for (infoButton in buttonArr)
                infoButton.text = ""
            popup.dismiss()
        }
        // register
        bankRegisterButton.setOnClickListener {
            var complete = true
            for (infoButton in buttonArr) { complete = complete && (!isEmpty(infoButton.text))}
            if (!complete) {
                DynamicToast.makeError(activity!!.applicationContext, "정보입력이 완료되지 않았습니다!").show()
            }
            // when input deposit amount is 0
            else if (buttonArr[3].text == ("0원")){
                DynamicToast.makeError(activity!!.applicationContext, "입금하시는 금액은 0원일 수 없습니다!").show()
            }
            else {
                // get texts in each buttons in popup and make a Bank object
                val bankToBeSaved = Bank(
                        bankNameButton.text.toString(),
                        LocalDate.parse(bankStartDateButton.text),
                        LocalDate.parse(bankEndDateButton.text),
                        bankDepositAmountButton.text.toString()
                                .removeSuffix("원")
                                .replace(",","", false).toInt(),
                        parseDouble(bankInterestButton.text.toString().removeSuffix("%")),
                        bankDepositAmountButton.text.toString()
                                .removeSuffix("원")
                                .replace(",","", false).toInt()
                                * DateCalc.calcDepositMonth(LocalDate.parse(bankStartDateButton.text), LocalDate.now())
                )

                saveBankData(activity!!.applicationContext, bankToBeSaved, this.view!!, bankRecyclerView)
                updateRecyclerView(activity!!.applicationContext, bankRecyclerView)
                for (infoButton in buttonArr)
                    infoButton.text = ""
                popup.dismiss()
            }
        }
    }


    private fun saveBankData(context: Context, inputBank: Bank, view: View, recyclerView: RecyclerView) {
        val prefs = context.getSharedPreferences(BANK_PREFS_NAME, MODE_PRIVATE)
        val prefsEditor = prefs.edit()
        val jsonString = Gson().toJson(inputBank)

        // when the bank type is already contained
        // show pop-up whether to update or not
        if(prefs.all.containsKey(BANK_PREF_PREFIX_KEY + inputBank.bankName.hashCode())){
            val popupHasBankView = context.layoutInflater.inflate(R.layout.popup_has_bank, null)
            val popupHasBank = PopupWindow(popupHasBankView)
            popupHasBank.isFocusable = true
            popupHasBank.animationStyle = animStyle
            popupHasBank.showAtLocation(view, Gravity.CENTER, 0,0)
            popupHasBank.update(
                view,
                view.resources.displayMetrics.widthPixels,
                view.resources.displayMetrics.heightPixels)

            val popupHasBankCancel = popupHasBankView.find<Button>(R.id.popupHasBankCancel)
            val popupHasBankRegister = popupHasBankView.find<Button>(R.id.popupHasBankRegister)

            popupHasBankCancel.setOnClickListener { popupHasBank.dismiss() }
            popupHasBankRegister.setOnClickListener {
                prefsEditor.putString(BANK_PREF_PREFIX_KEY + inputBank.bankName.hashCode(), jsonString)
                prefsEditor.apply()
                updateRecyclerView(context, recyclerView)
                popupHasBank.dismiss()
            }

        } else {
            prefsEditor.putString(BANK_PREF_PREFIX_KEY + inputBank.bankName.hashCode(), jsonString)
            prefsEditor.apply()
        }
    }

    private fun loadBankData(context: Context): ArrayList<Bank> {
        val prefs = context.getSharedPreferences(BANK_PREFS_NAME, MODE_PRIVATE)
        val arrayList = ArrayList<Bank>()

        // retrieve a map containing all data saved, save values as arrayList
        val allEntries = prefs.all
        for(keys in allEntries.keys){
            val bankInfo = Gson().fromJson(prefs.getString(keys, ""), Bank::class.java)
            arrayList.add(bankInfo)
        }
        return arrayList
    }

    private fun deleteBankData(context: Context, inputBank: Bank) {
        val prefs = context.getSharedPreferences(BANK_PREFS_NAME, MODE_PRIVATE).edit()
        prefs.remove(BANK_PREF_PREFIX_KEY + inputBank.bankName.hashCode())
        prefs.apply()
    }

    private fun updateRecyclerView(context:Context, view: RecyclerView) {
        // add recyclerView
        val arrayBankList = loadBankData(context)
        val adapter = BankRvAdapter(context, arrayBankList)
        view.adapter = adapter

        // add layoutManager
        val lm = LinearLayoutManager(context)
        view.layoutManager = lm
        view.setHasFixedSize(true)

        // update deposit information
        monthlySum = 0; bankTotalSum = 0
        for (i in 0 until arrayBankList.size){
            // update bankTotalDeposit
            arrayBankList[i].bankTotalDeposit =
                    arrayBankList[i].monthDeposit * DateCalc.calcDepositMonth(arrayBankList[i].startDate, LocalDate.now())
            // sum all monthly deposit
            monthlySum += arrayBankList[i].monthDeposit
            // sum all bankTotalDeposit
            bankTotalSum += arrayBankList[i].bankTotalDeposit
        }
        totalMonthlyDeposit.text = "월별 총 ${decimalFormat.format(monthlySum)}원"
        totalDeposit.text = "총 ${decimalFormat.format(bankTotalSum)}원"

        // when edit/delete button is onClicked
        adapter.setOnItemClickListener(object :BankRvAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                val popupEditMenu = PopupMenu(context, v)
                popupEditMenu.menuInflater.inflate(R.menu.bank_recycler_edit_menu, popupEditMenu.menu)
                popupEditMenu.setOnMenuItemClickListener {
                    when ((it.itemId)) {
                        R.id.bankRecyclerEdit -> showBigPopUp(position)
                        else -> {
                            deleteBankData(context, arrayBankList[position])
                            updateRecyclerView(context, view)
                        }
                    }
                    true
                }
                popupEditMenu.show()
            }
        })
        updateScoreView()
    }

    private fun updateScoreView() {
        val rankString = DateCalc.rankString(userInfo.rank, userInfo.affiliation)
        val rankIncome = DateCalc.rankIncome(userInfo)
        val depositPercent = 100.0f * monthlySum/rankIncome
        val score = when (depositPercent.toInt()) {
            0 -> "?"
            in 1 .. 30 -> "C"
            in 30 .. 50 -> "B"
            in 50 .. 70 -> "A"
            else -> "S"
        }
        val rankIncomeFormatted = decimalFormat.format(rankIncome) + "원"
        val depositPercentFormatted = "%.2f".format(depositPercent)

        depositScore.text = score
        depositScore.textColorResource = when (score) {
            "?" -> R.color.buttonColor3
            "C" -> R.color.CoolBrownDark
            "B" -> R.color.horizontalProgressbarGreen
            "A" -> R.color.horizontalProgressbarBlue
            else -> R.color.horizontalProgressbarRed
        }
        depositScoreText.text = when (score) {
            "?" -> "적금을 안드셨군요!"
            "C" -> "적당하군요!"
            "B" -> "좋아요!"
            "A" -> "훌륭해요!"
            else -> "정말 대단해요!"
        }
        depositScoreDetailPercent.text =
            "월급의 ${depositPercentFormatted}%를 저금 중입니다!"
        depositScoreDetailAmount.text =
            "(${rankString} 월급 ${rankIncomeFormatted} 중 적금 ${decimalFormat.format(monthlySum)}원)"
    }
}

