package com.kyminbb.militarycalendar.activities.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kyminbb.militarycalendar.R
import kotlinx.android.synthetic.main.fragment_calendar.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var adding = true
/**
 * A simple [Fragment] subclass.
 *
 */
class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    // Update UI after views are created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonAdd.setOnClickListener{
            if(adding) {
                addTab.setVisibility(View.VISIBLE)
            }
            else{
                addTab.setVisibility(View.GONE)
            }
            adding = !adding
        }

        addLeave.setOnClickListener{
            testText.text = "휴가 추가"
        }
        addDuty.setOnClickListener{
            testText.text = "당직 추가"
        }
        addExercise.setOnClickListener{
            testText.text = "훈련 추가"
        }
        addPersonal.setOnClickListener{
            testText.text = "개인일정 추가"
        }
    }
}
