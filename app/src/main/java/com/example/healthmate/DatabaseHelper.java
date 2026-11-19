
package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HealthMate.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_RECORDS = "health_records";
    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_STEPS = "steps";
    public static final String COL_CALORIES = "calories";
    public static final String COL_WATER = "water";
    private static final String INDEX_DATE = "index_health_records_date";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_RECORDS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DATE + " TEXT NOT NULL, "
                + COL_STEPS + " INTEGER NOT NULL, "
                + COL_CALORIES + " INTEGER NOT NULL, "
                + COL_WATER + " INTEGER NOT NULL"+ ")";
        db.execSQL(createTable);
        createDateIndex(db);
    }
    private void createDateIndex(SQLiteDatabase db) {
        String createIndex = "CREATE INDEX IF NOT EXISTS " + INDEX_DATE + " ON " + TABLE_RECORDS +
                "(" + COL_DATE + ")";
        db.execSQL(createIndex);
    }

    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        if (oldVersion < 2) {
            createDateIndex(db);
        }
    }
    private boolean isValidRecord(String date,int steps,int calories,int water) {
        return date != null && !date.trim().isEmpty() && steps >= 0 && calories >= 0 && water >= 0;
    }
    public boolean insertRecord( String date,int steps,int calories,int water) {
        if (!isValidRecord(date, steps, calories, water)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date.trim());
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);
        long result = db.insert(TABLE_RECORDS,null,values);
        return result != -1;
    }
    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,null,null,null,null,null,
                COL_DATE + " DESC, " + COL_ID + " DESC");
    }
    public Cursor getTodayRecord(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,null,COL_DATE + " = ?",new String[]{date},null,
                null,COL_ID + " DESC");
    }
    public Cursor getRecordById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,null,COL_ID + " = ?",new String[]{String.valueOf(id)},
                null,null,null);
    }

    public boolean updateRecord(int id,String date,int steps,int calories,int water) {
        if (id <= 0 || !isValidRecord(date, steps, calories, water)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date.trim());
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);
        int result = db.update(TABLE_RECORDS,values,COL_ID + " = ?",new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteRecord(int id) {
        if (id <= 0) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_RECORDS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }
    public Cursor getRecordsForDateRange(
            String startDate,
            String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_RECORDS,
                null,
                COL_DATE + " >= ? AND " + COL_DATE + " <= ?",
                new String[]{startDate, endDate},
                null,
                null,
                COL_DATE + " ASC, " + COL_ID + " ASC");
    }
}
