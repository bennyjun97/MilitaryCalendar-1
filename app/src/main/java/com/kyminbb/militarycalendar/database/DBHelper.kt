package com.kyminbb.militarycalendar.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import org.jetbrains.anko.db.*
import java.util.*


// Define the schema of the database.
object TableReaderContract {
    object TableEntry : BaseColumns {
        const val TABLE_NAME = "calendar"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_MEMO = "memo"
    }
}

//to save schedule for each days
object TableReaderForEachDays {
    object TableEntry2 : BaseColumns {
        const val TABLE_NAME = "calendar2"
        const val COLUMN_DATE = "date2"
        const val COLUMN_CONTENT = "content2"
        const val COLUMN_NAME = "name2"
        const val COLUMN_MEMO = "memo2"
    }
}

class DBHelper private constructor(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    init {
        instance = this
    }

    companion object {
        const val DATABASE_NAME = "calendar.db"
        const val DATABASE_VERSION = 1

        private var instance: DBHelper? = null

        @Synchronized
        fun getInstance(context: Context) = instance ?: DBHelper(context.applicationContext)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.createTable(
            TableReaderContract.TableEntry.TABLE_NAME,
            true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            TableReaderContract.TableEntry.COLUMN_START_DATE to TEXT,
            TableReaderContract.TableEntry.COLUMN_END_DATE to TEXT,
            TableReaderContract.TableEntry.COLUMN_CONTENT to TEXT,
            TableReaderContract.TableEntry.COLUMN_MEMO to TEXT
        )

        db!!.createTable(
            TableReaderForEachDays.TableEntry2.TABLE_NAME,
            true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            TableReaderForEachDays.TableEntry2.COLUMN_DATE to TEXT,
            TableReaderForEachDays.TableEntry2.COLUMN_CONTENT to TEXT,
            TableReaderForEachDays.TableEntry2.COLUMN_NAME to TEXT,
            TableReaderForEachDays.TableEntry2.COLUMN_MEMO to TEXT
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.dropTable(TableReaderContract.TableEntry.TABLE_NAME, true)

        db!!.dropTable(TableReaderForEachDays.TableEntry2.TABLE_NAME, true)
    }
}

