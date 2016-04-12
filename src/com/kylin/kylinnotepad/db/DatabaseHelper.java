package com.kylin.kylinnotepad.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.Note;

public class DatabaseHelper extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "kylinnotepad";
	
	private static final int VERSION = 1;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS "+
				Category.TABLE_NAME + "(" +
				Category.ID+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+
				Category.CATEGORY_NAME+" TEXT"+
				")");
		db.execSQL("CREATE TABLE IF NOT EXISTS "+
				Note.TABLE_NAME + "(" +
				Note.ID+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+
				Note.CATEGORY_ID+" INTEGER,"+
				Note.TITLE+" TEXT,"+
				Note.CONTENT+" TEXT,"+
				Note.LAST_EDIT_TIME+" INTEGER,"+
				Note.AUDIO_PATH+" TEXT,"+
				Note.VIDEO_PATH+" TEXT,"+
				Note.LOCALTION_X+" REAL,"+
				Note.LOCALTION_Y+" REAL,"+
				Note.LOCALTION_NAME+" TEXT"+
				")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ Category.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ Note.TABLE_NAME);
		onCreate(db);
	}

}
