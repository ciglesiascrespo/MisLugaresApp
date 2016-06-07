package com.example.ciglesias_pc.pruebasainet.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Ciglesias-pc on 05/06/2016.
 */
public class LugaresConfiguracion {

    public static final int LUGARES_CONFIGURACION_ID_USUARIO = 1;
    public static final int LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO = 2;
    public static final int LUGARES_CONFIGURACION_ID_TIPO_LOGIN = 3;
    public static final int LUGARES_CONFIGURACION_ID_NOMBRE = 4;

    // tipos de Login
    public static final int TIPO_LOGIN_SERVIDOR = 1;
    public static final int TIPO_LOGIN_FACEBOOK = 2;

    // tipos de Login
    public static final String LOGIN_ACTIVO = "1";
    public static final String LOGIN_INACTIVO = "0";

    public static final String ultimoNombreConfiguracion = "NombreUsuario";


    public static void updateLugaresConfiguracion(Context context, String valor, int id) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("valor", valor);
        int update = -1;
        update = resolver.update(LugaresProvider.CONTENT_URI_CONFIGURACION, cv,
                "_id=?", new String[]{String.valueOf(id)});

        Log.i("Update", "Actualizar id: " + id);
        Log.i("Update", "Filas actualilzadas: " + update);

    }

    public static void createTableLugaresConfiguracion(Context context) {

        Cursor c = null;
        ContentResolver resolver = context.getContentResolver();
        ;

        String sql = " CREATE TABLE  IF NOT EXISTS [configuracion] (" +
                " 	[_id] integer NOT NULL PRIMARY KEY AUTOINCREMENT,                                                         " +
                " 	[nombre] nvarchar(254),                                                                                   " +
                " 	[valor] nvarchar(254)                                                                                     " +
                " ) ;                                                                                                          ";

        try {

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {

                Log.e("create", c.getColumnNames().toString());
            }
        } catch (SQLiteException e) {
            Log.e("Error en base de datos", e.getMessage());
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
                Log.e("create", "Tabla creada con exito");

            }

        }

    }

    public static void dropLugaresConfiguracion(Context context) {

        ContentResolver resolver = context.getContentResolver();

        Cursor c = null;

        try {
            String sql = "Drop Table configuracion ";

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);
        } catch (SQLiteException e) {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            Log.e("dropTable", "Error eliminando tabla" + e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            Log.e("dropTable", "Tabla eliminada con exito");
        }
    }


    public static void insertRegistrosIniciales(Context context) {


        ContentResolver resolver = context.getContentResolver();
        Cursor c = null;

        HashMap<Integer, String> mapRegistrosValor = new HashMap<Integer, String>();
        HashMap<Integer, String> mapRegistrosNombre = new HashMap<Integer, String>();

        mapRegistrosNombre.put(LUGARES_CONFIGURACION_ID_USUARIO, "Usuario");
        mapRegistrosValor.put(LUGARES_CONFIGURACION_ID_USUARIO, "");

        mapRegistrosNombre.put(LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO, "LoginActivo");
        mapRegistrosValor.put(LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO, "0");

        mapRegistrosNombre.put(LUGARES_CONFIGURACION_ID_TIPO_LOGIN, "TipoLogin");
        mapRegistrosValor.put(LUGARES_CONFIGURACION_ID_TIPO_LOGIN, "0");

        mapRegistrosNombre.put(LUGARES_CONFIGURACION_ID_NOMBRE, "NombreUsuario");
        mapRegistrosValor.put(LUGARES_CONFIGURACION_ID_NOMBRE, "0");
        try {


            String sql = "Select nombre  from configuracion order by _id desc limit 1";
            String ultimoNombreConfiguracionDb = "";

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndex("nombre"))) {
                    ultimoNombreConfiguracionDb = c.getString(c.getColumnIndex("nombre"));
                }
            }


            if (!ultimoNombreConfiguracionDb.equals(ultimoNombreConfiguracion)) {

                dropLugaresConfiguracion(context);
                createTableLugaresConfiguracion(context);

                ContentValues cv = new ContentValues();

                for (int i = 1; i <= mapRegistrosNombre.size(); i++) {
                    cv.put("nombre", mapRegistrosNombre.get(i));
                    cv.put("valor", mapRegistrosValor.get(i));

                    resolver.insert(LugaresProvider.CONTENT_URI_CONFIGURACION, cv);
                }

            }


        } catch (SQLiteException e) {
            Log.e("InsertRegistros", e.getMessage());

        } finally {

            Log.e("InsertRegistros", "Datos insertados con exito");

        }
    }

    public static boolean isLoginActivo(Context context) {
        boolean result = false;

        String sql = "select valor from configuracion where _id= " + LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO;
        Cursor c = null;
        ContentResolver resolver = context.getContentResolver();

        try {

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndex("valor"))) {
                    result = c.getString(c.getColumnIndex("valor")).equals(String.valueOf(LOGIN_ACTIVO));
                }
            }

        } catch (Exception e) {
            Log.e("isLoginActivo", "Error: " + e.getMessage());
            if (c != null && !c.isClosed()) {
                c.close();
            }
            return false;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }

        return result;
    }

    public static String getCorreoUsuario(Context context){
        String result = "";

        String sql = "select valor from configuracion where _id= " + LUGARES_CONFIGURACION_ID_NOMBRE;
        Cursor c = null;
        ContentResolver resolver = context.getContentResolver();

        try {

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndex("valor"))) {
                    result = c.getString(c.getColumnIndex("valor"));
                }
            }

        } catch (Exception e) {
            Log.e("getIdUsuario", "Error: " + e.getMessage());
            if (c != null && !c.isClosed()) {
                c.close();
            }
            return "";
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }

        return result;
    }
    public static String getIdUsuario(Context context){
        String result = "";

        String sql = "select valor from configuracion where _id= " + LUGARES_CONFIGURACION_ID_USUARIO;
        Cursor c = null;
        ContentResolver resolver = context.getContentResolver();

        try {

            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndex("valor"))) {
                    result = c.getString(c.getColumnIndex("valor"));
                }
            }

        } catch (Exception e) {
            Log.e("getIdUsuario", "Error: " + e.getMessage());
            if (c != null && !c.isClosed()) {
                c.close();
            }
            return "";
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }

        return result;
    }

    public static HashMap<String, String> getParametrosLoginFromDb(Context context) {
        HashMap result = new HashMap();

        String sql = "select * from ( ";
        sql += "(select valor as Correo from configuracion where _id = " + LUGARES_CONFIGURACION_ID_USUARIO + "),";
        sql += "(select valor as TipoLogin from configuracion where _id = " + LUGARES_CONFIGURACION_ID_TIPO_LOGIN + "),";
        sql += "(select valor as NombreUsuario from configuracion where _id = " + LUGARES_CONFIGURACION_ID_NOMBRE + "))";

        Cursor c = null;
        ContentResolver resolver = context.getContentResolver();

        try {
            String correo="", tipoLogin="",nombreUsuario ="";
            c = resolver.query(LugaresProvider.CONTENT_URI_CONFIGURACION, null, sql, null, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndex("Correo"))) {
                    correo = c.getString(c.getColumnIndex("Correo"));
                }
                if (!c.isNull(c.getColumnIndex("TipoLogin"))) {
                    tipoLogin = c.getString(c.getColumnIndex("TipoLogin"));
                }
                if (!c.isNull(c.getColumnIndex("NombreUsuario"))) {
                    nombreUsuario = c.getString(c.getColumnIndex("NombreUsuario"));
                }

                result.put("Correo",correo);
                result.put("TipoLogin",tipoLogin);
                result.put("NombreUsuario",nombreUsuario);
            }

        } catch (Exception e) {
            Log.e("isLoginActivo", "Error: " + e.getMessage());
            if (c != null && !c.isClosed()) {
                c.close();
            }
            return null;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }

        return result;
    }

}
