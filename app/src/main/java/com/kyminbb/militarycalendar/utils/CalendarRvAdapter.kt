package com.kyminbb.militarycalendar.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kyminbb.militarycalendar.R
import org.jetbrains.anko.imageResource


class CalendarRvAdapter(val context: Context, private val eventList: List<Event>)
    :  RecyclerView.Adapter<CalendarRvAdapter.Holder>() {


    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    // 리스너 객체 참조를 저장하는 변수
    private var mListener: OnItemClickListener? = null

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_recycler_view_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(eventList[position], context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        val schedule = itemView?.findViewById<TextView>(R.id.ScheduleType)
        val startText = itemView?.findViewById<TextView>(R.id.startText)
        val endText = itemView?.findViewById<TextView>(R.id.endText)
        val nameText = itemView?.findViewById<TextView>(R.id.nameText)
        val scheduleSetting = itemView?.findViewById<ImageButton>(R.id.scheduleSetting)

        init{
            scheduleSetting!!.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    if(mListener != null){
                        mListener!!.onItemClick(scheduleSetting, position)
                    }
                }
            }
        }


        @SuppressLint("ResourceAsColor")
        fun bind (event: Event, context: Context){
            when(event.eventContent) {
                "휴가" -> {
                    startText?.setTextColor(Color.parseColor("#7CB342"))
                    endText?.setTextColor(Color.parseColor("#7CB342"))
                    schedule?.setTextColor(Color.parseColor("#7CB342"))
                    nameText?.setTextColor(Color.parseColor("#7CB342"))
                }
                "당직" -> {
                    startText?.setTextColor(Color.parseColor("#FF5722"))
                    endText?.setTextColor(Color.parseColor("#FF5722"))
                    schedule?.setTextColor(Color.parseColor("#FF5722"))
                    nameText?.setTextColor(Color.parseColor("#FF5722"))
                }
                "훈련" -> {
                    startText?.setTextColor(Color.parseColor("#9E9D24"))
                    endText?.setTextColor(Color.parseColor("#9E9D24"))
                    schedule?.setTextColor(Color.parseColor("#9E9D24"))
                    nameText?.setTextColor(Color.parseColor("#9E9D24"))
                }
                "개인" -> {
                    startText?.setTextColor(Color.parseColor("#64B5F6"))
                    endText?.setTextColor(Color.parseColor("#64B5F6"))
                    schedule?.setTextColor(Color.parseColor("#64B5F6"))
                    nameText?.setTextColor(Color.parseColor("#64B5F6"))
                }
            }
            scheduleSetting?.imageResource = R.drawable.small_menu_button
            /* Textview 와 String data를 연결한다 */
            startText?.text = event.eventStartDate
            endText?.text = event.eventEndDate
            schedule?.text = event.eventContent
            nameText?.text = event.eventName
        }
    }
}
