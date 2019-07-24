package com.kyminbb.militarycalendar.utils

import com.commit451.addendum.threetenabp.toLocalDate
import org.threeten.bp.LocalDate
import java.util.*

data class Bank(
    var bankName: String = "",
    var startDate: LocalDate = Calendar.getInstance().toLocalDate(),
    var endDate: LocalDate = Calendar.getInstance().toLocalDate(),
    var monthDeposit: String = "",
    var interest: Double = 0.0
)