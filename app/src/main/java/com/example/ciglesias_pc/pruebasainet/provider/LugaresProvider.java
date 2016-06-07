package com.example.ciglesias_pc.pruebasainet.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LugaresProvider extends ContentProvider {


    // Helper
    LugaresHelper myHelper;

    public final static String AUTHORITY = "provider.LugaresProvider";

    // Ids de las tablas a usar
    private static final int CONFIGURACION = 1;


    // nombre de las tablas a usar
    private static final String TABLA_CONFIGURACION = "configuracion";


    // URIS
    public final static Uri CONTENT_URI_CONFIGURACION = Uri.parse("content://" + AUTHORITY + "/" + TABLA_CONFIGURACION);


    // Uri Matcher

    public static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLA_CONFIGURACION, CONFIGURACION);

    }


    public LugaresProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows = 0;
        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = myHelper.getWritableDatabase();
        switch (match) {
            case CONFIGURACION:
                rows = db.delete(TABLA_CONFIGURACION, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.

        ContentValues contentValues;
        if (values != null) {
            contentValues = new ContentValues(values);
        } else {
            contentValues = new ContentValues();
        }

        // Inserción de nueva fila
        SQLiteDatabase db = myHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        Uri uri_actividad = null;
        long rowId;
        switch (match) {
            case CONFIGURACION:
                rowId = db.insert(TABLA_CONFIGURACION,
                        null, contentValues);
                if (rowId > 0) {
                    uri_actividad =
                            ContentUris.withAppendedId(
                                    CONTENT_URI_CONFIGURACION, rowId);
                    getContext().getContentResolver().
                            notifyChange(uri_actividad, null);

                }
                break;

            default:
                throw new SQLException("Falla al insertar fila en : " + uri);
        }

        return uri_actividad;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        myHelper = new LugaresHelper(getContext(), "LugaresDb.db", null, 1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor c = null;
        // Obtener base de datos
        SQLiteDatabase db = myHelper.getWritableDatabase();

        try {
            c = db.rawQuery(selection, selectionArgs);
            c.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URI no soportada: " + uri);
        }

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int rows = 0;
        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = myHelper.getWritableDatabase();
        switch (match) {
            case CONFIGURACION:
                rows = db.update(TABLA_CONFIGURACION, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }
}
