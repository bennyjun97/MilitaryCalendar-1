package com.kyminbb.militarycalendar.database

import android.content.Context
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select

class CalendarDB(context: Context) {
    private val dbHelper = DBHelper.getInstance(context)

    // Store a schedule on the designated date into the table.
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

    fun readDB(startDate: String): List<Triple<String, String, String>> {
        return dbHelper.use {
            select(
                TableReaderContract.TableEntry.TABLE_NAME,
                TableReaderContract.TableEntry.COLUMN_END_DATE,
                TableReaderContract.TableEntry.COLUMN_CONTENT,
                TableReaderContract.TableEntry.COLUMN_MEMO
            ).whereSimple("${TableReaderContract.TableEntry.COLUMN_START_DATE} = ?", startDate).exec {
                val parser = rowParser { endDate: String, content: String, memo: String ->
                    Triple(endDate, content, memo)
                }
                parseList(parser)
            }
        }
    }
}