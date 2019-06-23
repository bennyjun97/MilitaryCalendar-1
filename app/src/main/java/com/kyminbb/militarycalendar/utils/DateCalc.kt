package com.kyminbb.militarycalendar.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

object DateCalc {
    //입대날짜, 보직에 따라 전역날짜 계산해준다.
    fun calcETS(date: LocalDate, affiliation: String?): LocalDate {
        when (affiliation) {
            "육군", "의경" -> return armyETS(date)
            "해군", "해양의무경찰" -> return navyETS(date)
            "공군" -> return airETS(date)
            "해병" -> return marineETS(date)
            "사회복무요원" -> return agentETS(date)
        }
        // affiliation == "의무소방"
        return fireETS(date)
    }

    //일병 진급일
    fun calcRank2(date: LocalDate, affiliation: String?): LocalDate {
        when (affiliation) {
            "육군/의경", "해군/해양의무경찰", "해병", "사회복무요원" -> when {
                date.dayOfMonth == 1 -> return plus3Months(date)
                else -> return date.plusMonths(4).withDayOfMonth(1)
            }
            "공군" -> return plus3Months(date)
            else -> return date.plusMonths(4).withDayOfMonth(1)
        }
    }

    //상병 진급일
    fun calcRank3(date: LocalDate, affiliation: String?): LocalDate {
        return plus7Months(calcRank2(date, affiliation))
    }

    //병장 진급일
    fun calcRank4(date: LocalDate, affiliation: String?): LocalDate {
        return plus7Months(calcRank3(date, affiliation))
    }

    fun entirePercent(enlistDateTime: LocalDateTime, etsDateTime: LocalDateTime): Double {
        var now = LocalDateTime.now()
        if(now.isAfter(etsDateTime)) {
            return 100.0
        }
        else if(now.isBefore(enlistDateTime)) {
            return 0.0
        }
        var timeDif1 = ChronoUnit.SECONDS.between(enlistDateTime, etsDateTime).toDouble()
        var timeDif2 = ChronoUnit.SECONDS.between(enlistDateTime, now).toDouble()

        return (timeDif2 / timeDif1) * 100.0
    }

    fun rankPercent(startTime: LocalDateTime, endTime: LocalDateTime): Double {
        var now = LocalDateTime.now()
        var timeDif1 = ChronoUnit.MILLIS.between(startTime, endTime).toDouble()
        var timeDif2 = ChronoUnit.MILLIS.between(startTime, now).toDouble()

        return (timeDif1 / timeDif2) * 100.0
    }

    fun monthPercent(startTime: LocalDateTime, endTime: LocalDateTime) : Double {
        var now = LocalDateTime.now()
        var timeDif1 = ChronoUnit.MILLIS.between(startTime, endTime).toDouble()
        var timeDif2 = ChronoUnit.MILLIS.between(startTime, now).toDouble()

        return (timeDif1 / timeDif2) * 100.0
    }

    //육군 전역날짜 계산.
    private fun armyETS(date: LocalDate): LocalDate {
        //원래 21개월
        if (date.isBefore(LocalDate.parse("2017-01-03"))) {
            return plus21MonthsMinusOne(date)
        }
        //최종적으로 18개월
        else if (date.isAfter(LocalDate.parse("2020-06-02"))) {
            return plus18MonthsMinusOne(date)
        } else {
            val compDay = LocalDate.parse("2017-01-03")
            //1월 3일 입대자부터 2주에 하루씩 더 준다.
            return plus21MonthsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //해군 전역일 계산
    private fun navyETS(date: LocalDate): LocalDate {
        //원래 23개월
        if (date.isBefore(LocalDate.parse("2016-11-03"))) {
            return plus23MonthsMinusOne(date)
        }
        //최종적으로 20개월
        else if (date.isAfter(LocalDate.parse("2020-04-02"))) {
            return plus20MonthsMinusOne(date)
        } else {
            val compDay = LocalDate.parse("2016-11-03")
            //11월 3일 입대자부터 2주에 하루씩 준다.
            return plus23MonthsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //공군 전역일 계산
    private fun airETS(date: LocalDate): LocalDate {
        //원래 24개월
        if (date.isBefore(LocalDate.parse("2016-10-03"))) {
            return plus2YearsMinusOne(date)
        } else if (date.isAfter(LocalDate.parse("2020-01-02"))) {
            //최종적으로 22개월
            return plus22MonthsMinusOne(date)
        } else {
            val compDay = LocalDate.parse("2016-10-03")
            //10월 3일 입대자부터 2주에 하루씩 준다.
            return plus2YearsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }

    }

    //해병대 계산
    fun marineETS(date: LocalDate): LocalDate {
        return armyETS(date)
    }

    //공익 계산
    fun agentETS(date: LocalDate): LocalDate {
        //원래 24개월
        if (date.isBefore(LocalDate.parse("2016-10-03"))) {
            return plus2YearsMinusOne(date)
        } else if (date.isAfter(LocalDate.parse("2020-03-02"))) {
            //최종적으로 21개월
            return plus21MonthsMinusOne(date)
        } else {
            val compDay = LocalDate.parse("2016-10-03")
            //10월 3일부터 2주에 하루씩 준다.
            return plus2YearsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //의무소방 계산
    private fun fireETS(date: LocalDate): LocalDate {
        //찾아보니 해군이랑 동일
        return navyETS(date)
    }

    private fun plus3Months(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1 -> when {
                date.year % 4 == 0 -> return date.plusDays(91)
                else -> return date.plusDays(90)
            }
            2 -> when {
                date.year % 4 == 0 -> return date.plusDays(90)
                else -> return date.plusDays(89)
            }
            3, 5, 6, 7, 8, 10, 11 -> return date.plusDays(92)
            4, 9 -> return date.plusDays(91)
            else -> when {
                date.year % 4 == 3 -> return date.plusDays(91)
                else -> return date.plusDays(90)
            }
        }
    }

    private fun plus7Months(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1, 2 -> when {
                date.year % 4 == 0 -> return date.plusDays(213)
                else -> return date.plusDays(212)
            }
            3, 4, 5, 6 -> return date.plusDays(214)
            7 -> return date.plusDays(215)
            else -> when {
                date.year % 4 == 3 -> return date.plusDays(213)
                else -> return date.plusDays(212)
            }
        }
    }

    private fun plus18MonthsMinusOne(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1, 2 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(546)
                else -> return date.plusDays(545)
            }
            3, 5, 7, 8 -> when {
                date.year % 4 == 3 -> return date.plusDays(549)
                else -> return date.plusDays(548)
            }
            4, 6 -> when {
                date.year % 4 == 3 -> return date.plusDays(548)
                else -> return date.plusDays(547)
            }
            9, 11 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(546)
                else -> return date.plusDays(545)
            }
            10, 12 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(547)
                else -> return date.plusDays(546)
            }
            else -> return date.minusDays(1).plusMonths(18)
        }
    }

    private fun plus20MonthsMinusOne(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(608) else date.plusDays(607)
            2 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(607) else date.plusDays(606)
            3, 5, 6 -> return if (date.year % 4 == 3) date.plusDays(610) else date.plusDays(609)
            4 -> return if (date.year % 4 == 3) date.plusDays(609) else date.plusDays(608)
            7, 8, 10, 12 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(608) else date.plusDays(
                607
            )
            9, 11 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(607) else date.plusDays(606)
        }
        return date.plusMonths(20).minusDays(1)
    }

    private fun plus21MonthsMinusOne(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1, 2 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(638) else date.plusDays(637)
            3, 4 -> return if (date.year % 4 == 3) date.plusDays(640) else date.plusDays(639)
            5 -> return if (date.year % 4 == 3) date.plusDays(641) else date.plusDays(640)
            6, 8, 9, 10, 11 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(638) else date.plusDays(
                637
            )
            7, 12 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(639) else date.plusDays(638)
        }
        return date.plusMonths(21).minusDays(1)
    }

    private fun plus22MonthsMinusOne(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(669) else date.plusDays(668)
            2 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(668) else date.plusDays(667)
            3, 4 -> return if (date.year % 4 == 3) date.plusDays(671) else date.plusDays(670)
            5, 6, 7, 8, 10, 11, 12 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(669) else date.plusDays(
                668
            )
            9 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(668) else date.plusDays(667)
        }
        return date.plusMonths(22).minusDays(1)
    }

    private fun plus23MonthsMinusOne(date: LocalDate): LocalDate {
        when (date.monthValue) {
            1, 2 -> return if (date.year % 4 == 0 || date.year % 4 == 3) date.plusDays(699) else date.plusDays(698)
            3 -> return if (date.year % 4 == 3) date.plusDays(702) else date.plusDays(701)
            4, 6, 8, 9, 11 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(699) else date.plusDays(
                698
            )
            5, 7, 10, 12 -> return if (date.year % 4 == 2 || date.year % 4 == 3) date.plusDays(700) else date.plusDays(
                699
            )
        }
        return date.plusMonths(23).minusDays(1)
    }

    private fun plus2YearsMinusOne(date: LocalDate): LocalDate {
        if (date.monthValue == 2)
            if (date.dayOfMonth == 29)
                return date.minusDays(1).plusYears(2)
        return date.plusYears(2).minusDays(1)
    }
}