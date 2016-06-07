package com.example.ciglesias_pc.pruebasainet.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ciglesias-pc on 05/06/2016.
 */
public class LugaresHelper extends SQLiteOpenHelper {
    private Context mContext;
    private String DATA_BASE_NAME;
    private int DATA_BASE_VERSION;
    private static SQLiteDatabase myDataBase;

    public LugaresHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        DATA_BASE_NAME = name;
        DATA_BASE_VERSION = version;

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
