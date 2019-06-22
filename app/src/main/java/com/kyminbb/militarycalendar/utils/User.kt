package com.kyminbb.militarycalendar.utils

import com.commit451.addendum.threetenabp.toLocalDate
import org.threeten.bp.LocalDate
import java.util.*

// Initialize the user info
data class User(
    var name: String = "",
    var affiliation: String = "육군/의경",
    var profileImage: String = "",
    var rank: Int = 0,
    var promotionDates: MutableList<LocalDate> = MutableList(5, { Calendar.getInstance().toLocalDate() })
)

