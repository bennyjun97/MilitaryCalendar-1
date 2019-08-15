package com.kyminbb.militarycalendar.database

import android.content.Context
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select

data class Schedule(val startDate: String, val endDate: String, val content: String, val memo: String)

class CalendarDB(context: Context) {
    private val dbHelper = DBHelper.getInstance(context)

    // Store a schedule during the designated period into the table.
    fun writeDB(startDate: String, endDate: String, content: String, memo: String) {
        dbHelper.use {
            insert(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_START_DATE to startDate,
                TableReaderContract.TableEntry.COLUMN_END_DATE to endDate,
                TableReaderContract.TableEntry.COLUMN_CONTENT to content,
                TableReaderContract.TableEntry.COLUMN_MEMO to memo
            )
        }
    }

    // Extract a list of schedules that meet the given condition.
    fun readDB(filter: String, filterType: String): List<Schedule> {
        return dbHelper.use {
            val total = select(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_START_DATE,
                TableReaderContract.TableEntry.COLUMN_END_DATE,
                TableReaderContract.TableEntry.COLUMN_CONTENT,
                TableReaderContract.TableEntry.COLUMN_MEMO
            )
            when (filterType) {
                // Query schedules on the given date and order based on their end dates.
                "Date" -> total.whereSimple(
                    "${TableReaderContract.TableEntry.COLUMN_START_DATE} = ?",
                    filter
                ).orderBy(TableReaderContract.TableEntry.COLUMN_END_DATE)
                // Query all schedules in the given month and order chronologically.
                "Month" -> total.whereSimple(
                    "${TableReaderContract.TableEntry.COLUMN_START_DATE} = ?",
                    "$filter%"
                ).orderBy(TableReaderContract.TableEntry.COLUMN_START_DATE)
            }
            total.exec {
                val parser = rowParser { startDate: String, endDate: String, content: String, memo: String ->
                    Schedule(startDate, endDate, content, memo)
                }
                parseList(parser)
            }
        }
    }
}