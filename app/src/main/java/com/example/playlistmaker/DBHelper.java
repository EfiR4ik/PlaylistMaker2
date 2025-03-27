package com.example.playlistmaker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper databaseHelper;
    // Версия базы данных
    private static final int DATABASE_VERSION = 1;
    // Имя базы данных
    private static final String DATABASE_NAME = Config.DATABASE_NAME;
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static synchronized DBHelper getInstance(Context context){
        if(databaseHelper==null){
            databaseHelper = new DBHelper(context);
        }
        return databaseHelper;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
// Создание SQL-таблицы
        String CREATE_FILM_TABLE = "CREATE TABLE " + Config.TABLE_FILM + "("
                + Config.COLUMN_FILM_ID + " INTEGER PRIMARY KEY, "
                + Config.COLUMN_FILM_NAME + " TEXT NOT NULL, "
                + Config.COLUMN_FILM_DESCRIPTION + " TEXT, "
                + Config.COLUMN_FILM_IMAGE + " TEXT " //nullable
                + ")";
        db.execSQL(CREATE_FILM_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удали старую таблицу
        db.execSQL("DROP TABLE IF EXISTS " + Config.TABLE_FILM);
        // Создай таблицу ещё раз
        onCreate(db);
    }

}
