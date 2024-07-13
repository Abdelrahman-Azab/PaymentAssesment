package com.zeal.paymentassignment.core

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class ZealContentProvider : ContentProvider() {

    companion object {
        const val PROVIDER_NAME = "com.zeal.transaction.provider"
        const val URL = "content://$PROVIDER_NAME/transactions"
        val CONTENT_URI: Uri = Uri.parse(URL)

        private val values: HashMap<String, String>? = null

        private var db: SQLiteDatabase? = null

        const val id = "id"
        const val time = "time"
        const val cardNumber = "cardNumber"
        const val name = "name"
        const val amount = "total"
        const val discountAmount = "discountedAmount"
        const val uriCode = 1
        var uriMatcher: UriMatcher? = null

        const val DATABASE_NAME = "TransactionsDB"
        const val TABLE_NAME = "transactions"
        var DATABASE_VERSION = 5

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher!!.addURI(PROVIDER_NAME, "transactions", uriCode)
            uriMatcher!!.addURI(
                PROVIDER_NAME,
                "transactions/#",
                uriCode
            )
        }
    }

    override fun onCreate(): Boolean {
        val dbHelper = MyTransactionsDatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var sortOrder = sortOrder
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = TABLE_NAME

        when (uriMatcher?.match(uri)) {
            uriCode -> queryBuilder.projectionMap = values
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = id
        }

        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db?.insert(TABLE_NAME, "", values)
        if (rowID != null && rowID > 0) {
            val tempUri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context?.contentResolver?.notifyChange(tempUri, null)
            return tempUri
        }
        throw SQLException("Failed to add a record into $uri")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count = 0
        when (uriMatcher?.match(uri)) {
            uriCode ->
                count = db!!.update(TABLE_NAME, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher?.match(uri)) {
            uriCode ->
                count = db!!.delete(TABLE_NAME, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher?.match(uri)) {
            uriCode ->
                "vnd.android.cursor.dir/transactions"
            else -> {
                throw IllegalArgumentException("Unsupported URI: $uri")
            }
        }
    }

    private class MyTransactionsDatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        //sql query to create a new table
        private val CREATE_DB_TABLE =
            "CREATE TABLE $TABLE_NAME (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$cardNumber TEXT NOT NULL, " +
                    "$name TEXT NOT NULL, " +
                    "$time INTEGER NOT NULL, " +
                    "$discountAmount TEXT NOT NULL, " +
                    "$amount TEXT NOT NULL);"

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}
