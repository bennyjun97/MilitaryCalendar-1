package com.kyminbb.militarycalendar.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kyminbb.militarycalendar.R
import kotlinx.android.synthetic.main.recycler_view_item.*

class BankRvAdapter(val context: Context, val bankList: ArrayList<Bank>) :
    RecyclerView.Adapter<BankRvAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return bankList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(bankList[position], context)
    }

    inner class Holder(itemView: View?): RecyclerView.ViewHolder(itemView!!){
        val bankItemName = itemView?.findViewById<TextView>(R.id.bankItemName)
        val bankItemTotalDeposit = itemView?.findViewById<TextView>(R.id.bankItemTotalDeposit)
        val bankItemMonthDeposit = itemView?.findViewById<TextView>(R.id.bankItemMonthDeposit)
        //val bankItemSetting = itemView?.findViewById<Button>(R.id.bankItemSetting)

        fun bind (bank: Bank, context: Context){
            /* Textview 와 String data를 연결한다 */
            bankItemName?.text = bank.bankName
            //bankItemTotalDeposit?.text = bank.totalDeposit
            bankItemMonthDeposit?.text = bank.monthDeposit
        }
    }
}