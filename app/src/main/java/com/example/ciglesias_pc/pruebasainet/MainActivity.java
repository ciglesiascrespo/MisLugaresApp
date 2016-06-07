package com.example.ciglesias_pc.pruebasainet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.ciglesias_pc.pruebasainet.provider.LugaresConfiguracion;
import com.example.ciglesias_pc.pruebasainet.service.RestService;
import com.example.ciglesias_pc.pruebasainet.service.VolleySingleton;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;

    EditText edtCorreo, edtConfirmCorreo, edtCorreoLogin, edtPassLogin, edtPassRegistro, edtConfirmPassRegistro;
    TextInputLayout tilCorreo, tilConfirmCorreo, tilCorreoLogin, tilPassRegistro, tilConfirmPassRegistro, tilPassLogin;
    LinearLayout linearLoginContainer, linearRegistroContainer;
    TextView txtLogin, txtRegistro;

    HashMap<String, String> mapHeaders;

    ProgressDialog progressDialog;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        facebookSDKInitialize();
        setContentView(R.layout.activity_main);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Log.e("onCreate", "token: " + accessToken.getToken());
            Profile profile = Profile.getCurrentProfile();

            if (profile != null) {
                Log.e("onCreate", "name: " + profile.getName());
                iniciarActivity(accessToken.getUserId(), String.valueOf(LugaresConfiguracion.TIPO_LOGIN_FACEBOOK), profile.getName());
            }
        } else {
            Log.e("onCreate", "no login facebook");
        }

        if (LugaresConfiguracion.isLoginActivo(this)) {
            HashMap<String, String> mapValores = LugaresConfiguracion.getParametrosLoginFromDb(this);

            if (mapValores != null) {
                iniciarActivity(mapValores.get("Correo"), mapValores.get("TipoLogin"), mapValores.get("NombreUsuario"));
            }
        }


        edtCorreo = (EditText) findViewById(R.id.id_edt_correo);
        edtConfirmCorreo = (EditText) findViewById(R.id.id_edt_confirm_correo);

        edtPassRegistro = (EditText) findViewById(R.id.id_edt_pass);
        edtConfirmPassRegistro = (EditText) findViewById(R.id.id_edt_confirm_pass);

        edtCorreoLogin = (EditText) findViewById(R.id.id_edt_correo_login);
        edtPassLogin = (EditText) findViewById(R.id.id_edt_pass_login);

        tilCorreo = (TextInputLayout) findViewById(R.id.id_til_correo);
        tilConfirmCorreo = (TextInputLayout) findViewById(R.id.id_til_confirm_correo);

        tilPassRegistro = (TextInputLayout) findViewById(R.id.id_til_pass_registro);
        tilConfirmPassRegistro = (TextInputLayout) findViewById(R.id.id_til_confirm_pass_registro);

        tilCorreoLogin = (TextInputLayout) findViewById(R.id.id_til_correo_login);
        tilPassLogin = (TextInputLayout) findViewById(R.id.id_til_pass_login);

        txtLogin = (TextView) findViewById(R.id.id_txt_ingresar);
        txtRegistro = (TextView) findViewById(R.id.id_txt_registrar);

        linearLoginContainer = (LinearLayout) findViewById(R.id.id_login_container);
        linearRegistroContainer = (LinearLayout) findViewById(R.id.id_registro_container);


        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearRegistroContainer.setVisibility(View.GONE);
                cleanRegistroContainer();
                linearLoginContainer.setVisibility(View.VISIBLE);

            }
        });

        txtRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLoginContainer.setVisibility(View.GONE);
                cleanLoginContainer();
                linearRegistroContainer.setVisibility(View.VISIBLE);
            }
        });
        edtCorreo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isCorreoValido(String.valueOf(s))) {
                    tilCorreo.setError("Correo no válido");
                } else {
                    tilCorreo.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtConfirmCorreo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isCorreoValido(String.valueOf(s))) {
                    tilConfirmCorreo.setError("Correo no válido");
                } else {
                    if (String.valueOf(s).equals(edtCorreo.getText().toString())) {
                        tilConfirmCorreo.setError(null);
                    } else {
                        tilConfirmCorreo.setError("Los correos no coinciden");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtPassRegistro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 4) {
                    tilPassRegistro.setError("La contraseña debe tener minimo 4 caracteres");
                } else {
                    tilPassRegistro.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtConfirmPassRegistro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 4) {
                    tilConfirmPassRegistro.setError("La contraseña debe tener mínimo 4 caracteres");
                } else {
                    if (!String.valueOf(s).equals(edtPassRegistro.getText().toString())) {
                        tilConfirmPassRegistro.setError("Las contraseñas no coinciden");
                    } else {
                        tilConfirmPassRegistro.setError(null);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtCorreoLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isCorreoValido(String.valueOf(s))) {
                    tilCorreoLogin.setError("Correo no válido");
                } else {
                    tilCorreoLogin.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPassLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 4) {
                    tilPassLogin.setError("La contraseña debe tener mínimo 4 caracteres");
                } else {
                    tilPassLogin.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=utf-8");

        // Boton login
        findViewById(R.id.id_btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtCorreoLogin.getText().toString().length() > 0 && edtPassLogin.getText().toString().length() > 0) {
                    if (tilCorreoLogin.getError() == null && tilPassLogin.getError() == null) {
                        login();
                    } else {
                        edtCorreoLogin.requestFocus();
                    }
                }else{
                    Toast.makeText(context,"No se permiten campos vacios",Toast.LENGTH_SHORT).show();
                }


            }
        });


        // Boton registra
        findViewById(R.id.id_btn_registrarse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtConfirmCorreo.getText().toString().length() > 0 && edtConfirmPassRegistro.getText().toString().length() > 0) {
                    if (tilConfirmCorreo.getError() == null && tilConfirmPassRegistro.getError() == null) {
                        registrarUsuario(edtConfirmCorreo.getText().toString(), edtConfirmPassRegistro.getText().toString());
                    } else {
                        edtCorreo.requestFocus();
                    }
                }
            }
        });

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        getLoginDetails(loginButton);

        LugaresConfiguracion.createTableLugaresConfiguracion(getApplicationContext());
        LugaresConfiguracion.insertRegistrosIniciales(getApplicationContext());


    }

    private boolean isCorreoValido(String correo) {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches();
    }

    private void cleanRegistroContainer() {
        edtCorreo.setText("");
        edtConfirmCorreo.setText("");
        edtPassRegistro.setText("");
        edtConfirmPassRegistro.setText("");
    }

    private void cleanLoginContainer() {
        edtCorreoLogin.setText("");
        edtPassLogin.setText("");
    }

    private void login() {
        progressDialog = new ProgressDialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setMessage("Validando usuario");
        progressDialog.show();

        HashMap<String, Object> mapLogin = new HashMap<String, Object>();
        HashMap<String, String> mapParametrosLogin = new HashMap<String, String>();

        mapParametrosLogin.put(RestService.ParametroCorreoLogin, edtCorreoLogin.getText().toString());
        mapParametrosLogin.put(RestService.ParametroPassword, edtPassLogin.getText().toString());

        mapLogin.put(RestService.MetodoLogin, mapParametrosLogin);

        JSONObject jsonLoginRequest = new JSONObject(mapLogin);
        Log.e("login", "Request: " + jsonLoginRequest.toString());
        new RestService(getApplicationContext()).post(VolleySingleton.URI_SERVICE_PRUEBA, jsonLoginRequest.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Login", "Result: " + response.toString());
                procesarRespuesta(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(context, "Error validando usuario", Toast.LENGTH_SHORT).show();
                Log.e("Login", "Error: " + error.getMessage());
            }
        }, mapHeaders);
    }

    private void registrarUsuario(String mail, String pass) {
        progressDialog = new ProgressDialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setMessage("Registrando usuario");
        progressDialog.show();

        HashMap<String, Object> mapRegistro = new HashMap<String, Object>();
        HashMap<String, String> mapParametrosRegistro = new HashMap<String, String>();

        mapParametrosRegistro.put(RestService.ParametroCorreoRegistro, mail);
        mapParametrosRegistro.put(RestService.ParametroPassword, pass);

        mapRegistro.put(RestService.MetodoRegistro, mapParametrosRegistro);

        JSONObject jsonRegistroRequest = new JSONObject(mapRegistro);
//        Log.e("Registro", "Request: " + jsonRegistroRequest.toString());
        new RestService(getApplicationContext()).post(VolleySingleton.URI_SERVICE_PRUEBA, jsonRegistroRequest.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Registrar", "Result: " + response.toString());
                procesarRespuestaRegistro(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(context, "Error registrando usuario", Toast.LENGTH_SHORT).show();
                Log.e("Registrar", "Error: " + error.getMessage());
            }
        }, mapHeaders);

    }

    protected void facebookSDKInitialize() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    private void procesarRespuesta(JSONObject jsonObject) {
        try {
            JSONObject jsonRespuesta = jsonObject.getJSONObject("Respuesta");
            if (progressDialog.isShowing()) progressDialog.dismiss();
            if (jsonRespuesta.getString("Estado").equals("OK")) {
                iniciarActivity(edtCorreoLogin.getText().toString(), String.valueOf(LugaresConfiguracion.TIPO_LOGIN_SERVIDOR), edtCorreoLogin.getText().toString());

            } else {
                Toast.makeText(getApplicationContext(), "Usuario o contraseña inválidos", Toast.LENGTH_SHORT).show();
                edtCorreoLogin.requestFocus();
            }
        } catch (Exception e) {
            Log.e("ProcesarRta", "Error: " + e.getMessage());
        }
    }

    private void procesarRespuestaRegistro(JSONObject jsonObject) {
        try {
            JSONObject jsonRespuesta = jsonObject.getJSONObject("Respuesta");
            if (progressDialog.isShowing()) progressDialog.dismiss();
            if (jsonRespuesta.getString("Estado").equals("OK")) {
                iniciarActivity(edtConfirmCorreo.getText().toString(), String.valueOf(LugaresConfiguracion.TIPO_LOGIN_SERVIDOR), edtConfirmCorreo.getText().toString());

            } else {
                Toast.makeText(getApplicationContext(), "Error registrando usuario", Toast.LENGTH_SHORT).show();
                edtCorreo.requestFocus();
            }
        } catch (Exception e) {
            Log.e("ProcesarRta", "Error: " + e.getMessage());
        }
    }

    protected void getLoginDetails(LoginButton login_button) {

        // Callback registration
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult login_result) {

                GraphRequest request = GraphRequest.newMeRequest(
                        login_result.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.e("LoginActivity", response.toString() + " ; " + object.toString());

                                // Application code
                                try {
                                    String name = object.getString("name");
                                    String id = object.getString("id");

                                    iniciarActivity(id, String.valueOf(LugaresConfiguracion.TIPO_LOGIN_FACEBOOK),name);

                                    Log.e("profile", "name: " + name);
                                } catch (JSONException e) {
                                    Log.e("profile", "Error: " + e.getMessage());
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();

                Toast.makeText(getApplicationContext(), "Logueado", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                // code for cancellation
                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                //  code to handle error
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarActivity(String id, String tipoLogin, String correo) {

        LugaresConfiguracion.updateLugaresConfiguracion(getApplicationContext(), id, LugaresConfiguracion.LUGARES_CONFIGURACION_ID_USUARIO);
        LugaresConfiguracion.updateLugaresConfiguracion(getApplicationContext(), tipoLogin, LugaresConfiguracion.LUGARES_CONFIGURACION_ID_TIPO_LOGIN);
        LugaresConfiguracion.updateLugaresConfiguracion(getApplicationContext(), LugaresConfiguracion.LOGIN_ACTIVO, LugaresConfiguracion.LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO);
        LugaresConfiguracion.updateLugaresConfiguracion(getApplicationContext(), correo, LugaresConfiguracion.LUGARES_CONFIGURACION_ID_NOMBRE);

        Intent i = new Intent(MainActivity.this, MenuPrincipalActivity.class);
        i.putExtra("Correo", correo);
        i.putExtra("TipoLogin", tipoLogin);
        finish();
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.e("data", data.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        context = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }
}
