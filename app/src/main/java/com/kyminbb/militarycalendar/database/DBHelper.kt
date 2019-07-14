package com.kyminbb.militarycalendar.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns


// Define the schema of the database.
object TableReaderContract {
    object TableEntry : BaseColumns {
        const val TABLE_NAME = "calendar"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_CONTENT = "content"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${TableReaderContract.TableEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER_PRIMARY_KEY," +
            "${TableReaderContract.TableEntry.COLUMN_START_DATE} TEXT," +
            "${TableReaderContract.TableEntry.COLUMN_END_DATE} TEXT," +
            "${TableReaderContract.TableEntry.COLUMN_CONTENT} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${TableReaderContract.TableEntry.TABLE_NAME}"


class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    companion object {
        const val DATABASE_NAME = "calendar.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}