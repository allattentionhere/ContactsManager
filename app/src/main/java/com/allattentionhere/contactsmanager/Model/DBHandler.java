package com.allattentionhere.contactsmanager.Model;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

public class DBHandler {
    private static final String sDB_NAME = "database";
    private static final String sTABLE_NAME = "person";
    private static final int sDATABASE_VERSION = 1;

    private static final String sCREATE_DATABASE = " CREATE TABLE IF NOT EXISTS "
            + sTABLE_NAME
            + " ( _id integer primary key autoincrement, id integer, person varchar, name varchar , isfavorite integer);";

    private Context mContext;
    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSQLiteDatabase;
    private String LOG_TAG = "DBHandler";

    public DBHandler(Context context) {
        this.mContext = context;
        this.mDatabaseHelper = new DatabaseHelper(this.mContext);
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        Context mContext;

        DatabaseHelper(Context context) {
            super(context, sDB_NAME, null, sDATABASE_VERSION);
            this.mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(sCREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(mContext, "This will update the database.", Toast.LENGTH_LONG).show();

            db.execSQL("DROP TABLE IF EXISTS " + sTABLE_NAME);

            onCreate(db);

        }
    }

    public void open() {
        mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDatabaseHelper.close();
    }

    public void insertContacts(List<Person> list) {
        mSQLiteDatabase.execSQL("DELETE FROM " + sTABLE_NAME);
        ContentValues values = new ContentValues();

        for (Person p : list) {
            values.put("id", p.getId());
            values.put("name", p.getFirst_name().toUpperCase() + " " + p.getLast_name().toUpperCase());
            values.put("Person", new Gson().toJson(p));
//            values.put("isfavorite", (p.isFavorite() ? 1 : 0));
            mSQLiteDatabase.insert(sTABLE_NAME, null, values);
            Log.i(LOG_TAG, "Inserted " + p.getFirst_name() + " " + p.getLast_name()+"|isFav="+p.isFavorite());
        }
    }

    public void updateContact(Person p,int id) {
        ContentValues values = new ContentValues();
        values.put("id", p.getId());
        values.put("name", p.getFirst_name() + " " + p.getLast_name());
        values.put("Person", new Gson().toJson(p));
        values.put("isfavorite", (p.isFavorite() ? 1 : 0));

        String where = "id" + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        mSQLiteDatabase.update(sTABLE_NAME, values, where, whereArgs);

        Log.i(LOG_TAG, "Updated " + p.getFirst_name());
    }
    public void insertRandomPerson()
    {
        mSQLiteDatabase.execSQL("DELETE FROM " + sTABLE_NAME);
        ContentValues values = new ContentValues();

        String person = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        for(int i = 0; i < 100; i++)
        {
            values.put("id",i);
            values.put("Person", "p");
            values.put("isfavorite", 0);
            values.put("name", person.substring(random.nextInt(person.length())));
            mSQLiteDatabase.insert(sTABLE_NAME, null, values);
            Log.i(LOG_TAG , "Inserted " + person.substring(random.nextInt(person.length())));
        }
    }

    public Cursor selectData() {
        return mSQLiteDatabase.query(sTABLE_NAME, null, null, null, null, null, "person ASC");
    }

    public Cursor selectNames() {
        String[] col = {"_id,id,name,isfavorite"};
        return mSQLiteDatabase.query(sTABLE_NAME, col, null, null, null, null, "isfavorite DESC,name ASC");
    }

    public void deletePersonTable() {
        mSQLiteDatabase.delete(sTABLE_NAME, "1", null);
    }

}
