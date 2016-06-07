package com.example.ciglesias_pc.pruebasainet.service;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ciglesias-pc on 05/06/2016.
 */
public class RestService {


    private final Context contexto;

    // Parametros
    public static final String ParametroCorreoLogin = "Correo";
    public static final String ParametroCorreoRegistro = "Email";
    public static final String ParametroPassword = "Password";
    public static final String ParametroNombreLugar = "Nombre";
    public static final String ParametroDescripcionLugar = "Descripcion";
    public static final String ParametroFotoLugar = "Foto";
    public static final String ParametroCoordenadaLugar ="Coordenadas";
    public static final String ParametroIdUsuarioLugar = "IdUsuario";

    // metodos
    public static final String MetodoLogin = "IniciarSesion2";
    public static final String MetodoRegistro = "CrearUsuario2";
    public static final String MetodoCrearLugar = "CrearSitio2";
    public static final String MetodoCargarLugares = "{Buscar2:{}}";


    public RestService(Context contexto) {
        this.contexto = contexto;
    }



    public void post(String uri, String datos, Response.Listener<JSONObject> jsonListener,
                     Response.ErrorListener errorListener, final HashMap<String, String> cabeceras) {

        // Crear petición POST
        JsonObjectRequest peticion = new JsonObjectRequest(
                Request.Method.POST,
                uri,
                datos,
                jsonListener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return cabeceras;
            }
        };

        // Añadir petición a la pila
        VolleySingleton.getInstance(contexto).addToRequestQueue(peticion);
    }
}
