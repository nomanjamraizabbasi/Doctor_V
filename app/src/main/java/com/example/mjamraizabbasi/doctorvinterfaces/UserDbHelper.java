package com.example.mjamraizabbasi.doctorvinterfaces;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by M Jamraiz Abbasi on 2/25/2018.
 */

public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="doctorv_local";
    private static final int DATABASE_VERSION=1;
    private static final String CREATE_QUERY=
            "CREATE TABLE "+ userinfo_local.new_user.TABLE_NAME +
                    " ( " + userinfo_local.new_user.USER_NAME + " TEXT PRIMARY KEY ,"
                    + userinfo_local.new_user.USER_PASSWORD + " TEXT );";

    private static final String CREATE_QUERY2=
            "CREATE TABLE "+ medicalLog_local.new_record.TABLE_NAME +
                    " ( " + medicalLog_local.new_record.LOG_ID + " INT PRIMARY KEY ,"
                    + medicalLog_local.new_record.LOG_USER + " TEXT, "
                    + medicalLog_local.new_record.LOG_DATE + " TEXT, "
                    + medicalLog_local.new_record.LOG_TYPE + " TEXT, "
                    + medicalLog_local.new_record.LOG_DETAILS + " TEXT);";


    public UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //remember, you have to create both tables at the time db is created, cuz oncreate Is called only once when db is created, lemme show you

        Log.e("Database Operations", "Database created/opened");

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_QUERY);
        sqLiteDatabase.execSQL(CREATE_QUERY2);
        Log.e("Database Operations", "Table created");
    }

    public void add_newuser(String username, String pass, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues= new ContentValues();
        contentValues.put(userinfo_local.new_user.USER_NAME, username);
        contentValues.put(userinfo_local.new_user.USER_PASSWORD, pass);
        long answer = sqLiteDatabase.insert(userinfo_local.new_user.TABLE_NAME,null, contentValues);
        Log.e("Database Operations", Long.toString(answer));
    }

    public void add_newrecord(String username, String date, String type, String details,  SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues= new ContentValues();
        contentValues.put(medicalLog_local.new_record.LOG_USER, username);
        contentValues.put(medicalLog_local.new_record.LOG_DATE, date);
        contentValues.put(medicalLog_local.new_record.LOG_TYPE, type);
        contentValues.put(medicalLog_local.new_record.LOG_DETAILS, details);
        sqLiteDatabase.insert(medicalLog_local.new_record.TABLE_NAME,null, contentValues);
        Log.e("Database Operations", "Row inserted in logs");
    }

    //this is where i am retrieving my data
    public Cursor get_AllLogs(String username, SQLiteDatabase sqLiteDatabase){
        String [] projections= {medicalLog_local.new_record.LOG_DATE, medicalLog_local.new_record.LOG_TYPE};
        String selection= medicalLog_local.new_record.LOG_USER+" Like ?";
        String [] selection_arg= {username};
        Cursor cursor= sqLiteDatabase.query(medicalLog_local.new_record.TABLE_NAME,projections,selection,selection_arg,null,null,null);
        return cursor;
    }

    public Cursor get_SpecificLogs(String username, String type, SQLiteDatabase sqLiteDatabase){
        String [] selection_arg= {username,type};
        Cursor cursor=sqLiteDatabase.rawQuery("Select date, details from medical_logs where username = ? and type = ?", selection_arg );
        Log.e("Database Operations", "Row fetched from logs");
        return cursor;

    }

    public  Cursor checkLoginDetails(String username, SQLiteDatabase sqLiteDatabase){
        //String [] projections= {userinfo_local.new_user.USER_PASSWORD};
        //String selection= userinfo_local.new_user.USER_NAME+" Like ?";
        String [] selection_arg= {username};
        //Cursor cursor= sqLiteDatabase.query(userinfo_local.new_user.TABLE_NAME,projections,selection, selection_arg,null,null,null);
        Cursor cursor=sqLiteDatabase.rawQuery("select password from user_account where username = ? ", selection_arg);
        Log.e("Database Operations", Integer.toString(cursor.getCount()));
        return cursor;
    }

    public boolean updatePassword(String username, String password, SQLiteDatabase sqLiteDatabase){
        String selection= userinfo_local.new_user.USER_NAME+" Like ?";
        String [] selection_arg= {username};
        ContentValues contentValues= new ContentValues();
        contentValues.put(userinfo_local.new_user.USER_NAME, username);
        contentValues.put(userinfo_local.new_user.USER_PASSWORD, password);
        sqLiteDatabase.update(userinfo_local.new_user.TABLE_NAME, contentValues, selection,selection_arg);
        return true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + userinfo_local.new_user.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + medicalLog_local.new_record.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
