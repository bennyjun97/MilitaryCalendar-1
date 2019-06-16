package com.kyminbb.militarycalendar

import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

object dateCalc {
    //입대날짜, 보직에 따라 전역날짜 계산해준다.
    fun calcETS(date: LocalDate, affil: String?): LocalDate {
        when {
            affil.equals("육군/의경") -> return armyETS(date)
            affil.equals("해군/해양의무경찰") -> return navyETS(date)
            affil.equals("공군") -> return airETS(date)
            affil.equals("해병") -> return marineETS(date)
            affil.equals("사회복무요원") -> return agentETS(date)
            else -> return fireETS(date)
        }
    }

    //육군 전역날짜 계산.
    fun armyETS(date: LocalDate) : LocalDate {
        //원래 21개월
        if(date.isBefore(LocalDate.parse("2017-01-03"))) {
            return plus21MonthsMinusOne(date)
        }
        //최종적으로 18개월
        else if(date.isAfter(LocalDate.parse("2020-06-02"))) {
            return plus18MonthsMinusOne(date)
        }
        else {
            val compDay = LocalDate.parse("2017-01-03")
            //1월 3일 입대자부터 2주에 하루씩 더 준다.
            return plus21MonthsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //해군 전역일 계산
    fun navyETS(date: LocalDate) : LocalDate {
        //원래 23개월
        if(date.isBefore(LocalDate.parse("2016-11-03"))) {
            return plus23MonthsMinusOne(date)
        }
        //최종적으로 20개월
        else if(date.isAfter(LocalDate.parse("2020-04-02"))) {
            return plus20MonthsMinusOne(date)
        }
        else {
            val compDay = LocalDate.parse("2016-11-03")
            //11월 3일 입대자부터 2주에 하루씩 준다.
            return plus23MonthsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //공군 전역일 계산
    fun airETS(date: LocalDate) : LocalDate {
        //원래 24개월
        if(date.isBefore(LocalDate.parse("2016-10-03"))) {
            return plus2YearsMinusOne(date)
        }
        else if(date.isAfter(LocalDate.parse("2020-01-02"))) {
            //최종적으로 22개월
            return plus22MonthsMinusOne(date)
        }
        else {
            val compDay = LocalDate.parse("2016-10-03")
            //10월 3일 입대자부터 2주에 하루씩 준다.
            return plus2YearsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }

    }

    //해병대 계산
    fun marineETS(date: LocalDate) : LocalDate {
        return armyETS(date)
    }

    //공익 계산
    fun agentETS(date: LocalDate) : LocalDate {
        //원래 24개월
        if(date.isBefore(LocalDate.parse("2016-10-03"))) {
            return plus2YearsMinusOne(date)
        }
        else if(date.isAfter(LocalDate.parse("2020-03-02"))) {
            //최종적으로 21개월
            return plus21MonthsMinusOne(date)
        }
        else {
            val compDay = LocalDate.parse("2016-10-03")
            //10월 3일부터 2주에 하루씩 준다.
            return plus2YearsMinusOne(date).minusDays((ChronoUnit.DAYS.between(compDay, date) / 14) + 1)
        }
    }

    //의무소방 계산
    fun fireETS(date: LocalDate) : LocalDate {
        //찾아보니 해군이랑 동일
        return navyETS(date)
    }

    fun plus18MonthsMinusOne(date: LocalDate) : LocalDate {
        when(date.monthValue) {
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

    fun plus20MonthsMinusOne(date: LocalDate) : LocalDate {
        when(date.monthValue) {
            1 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(608)
                else -> return date.plusDays(607)
            }
            2 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(607)
                else -> return date.plusDays(606)
            }
            3, 5, 6 -> when {
                date.year % 4 == 3 -> return date.plusDays(610)
                else -> return date.plusDays(609)
            }
            4 -> when {
                date.year % 4 == 3 -> return date.plusDays(609)
                else -> return date.plusDays(608)
            }
            7, 8, 10, 12 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(608)
                else -> return date.plusDays(607)
            }
            9, 11 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(607)
                else -> return date.plusDays(606)
            }
            else -> return date.plusMonths(20).minusDays(1)
        }
    }

    fun plus21MonthsMinusOne(date: LocalDate) : LocalDate {
        when(date.monthValue) {
            1, 2 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(638)
                else -> return date.plusDays(637)
            }
            3, 4 -> when {
                date.year % 4 == 3 -> return date.plusDays(640)
                else -> return date.plusDays(639)
            }
            5 -> when {
                date.year % 4 == 3 -> return date.plusDays(641)
                else -> return date.plusDays(640)
            }
            6, 8, 9, 10, 11 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(638)
                else -> return date.plusDays(637)
            }
            7, 12 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(639)
                else -> return date.plusDays(638)
            }
            else -> return date.plusMonths(21).minusDays(1)
        }
    }

    fun plus22MonthsMinusOne(date: LocalDate): LocalDate {
        when(date.monthValue) {
            1 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(669)
                else -> return date.plusDays(668)
            }
            2 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(668)
                else -> return date.plusDays(667)
            }
            3, 4 -> when {
                date.year % 4 == 3 -> return date.plusDays(671)
                else -> return date.plusDays(670)
            }
            5, 6, 7, 8, 10, 11, 12 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(669)
                else -> return date.plusDays(668)
            }
            9 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(668)
                else -> return date.plusDays(667)
            }
            else -> return date.plusMonths(22).minusDays(1)
        }
    }

    fun plus23MonthsMinusOne(date: LocalDate) : LocalDate {
        when(date.monthValue) {
            1, 2 -> when {
                date.year % 4 == 0 || date.year % 4 == 3 -> return date.plusDays(699)
                else -> return date.plusDays(698)
            }
            3 -> when {
                date.year % 4 == 3 -> return date.plusDays(702)
                else -> return date.plusDays(701)
            }
            4, 6, 8, 9, 11 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(699)
                else -> return date.plusDays(698)
            }
            5, 7, 10, 12 -> when {
                date.year % 4 == 2 || date.year % 4 == 3 -> return date.plusDays(700)
                else -> return date.plusDays(699)
            }
            else -> return date.plusMonths(23).minusDays(1)
        }
    }

    fun plus2YearsMinusOne(date: LocalDate) : LocalDate {
        if(date.monthValue == 2)
            if(date.dayOfMonth == 29)
                return date.minusDays(1).plusYears(2)
        return date.plusYears(2).minusDays(1)
    }
}