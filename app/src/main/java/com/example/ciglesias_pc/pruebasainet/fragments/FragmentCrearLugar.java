package com.example.ciglesias_pc.pruebasainet.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.ciglesias_pc.pruebasainet.R;
import com.example.ciglesias_pc.pruebasainet.Utils.ImageFilePath;
import com.example.ciglesias_pc.pruebasainet.Utils.ImageUtils;
import com.example.ciglesias_pc.pruebasainet.provider.LugaresConfiguracion;
import com.example.ciglesias_pc.pruebasainet.service.RestService;
import com.example.ciglesias_pc.pruebasainet.service.VolleySingleton;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Ciglesias-pc on 06/06/2016.
 */
public class FragmentCrearLugar extends Fragment implements LocationListener {
    Context context;

    ImageView imgTomarfoto;
    private final int RESULT_OK = -1;
    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private static final int REQUEST_SELECT_IMAGE = 2;

    public static final String directorioFoto = "/sdcard/MisLugares";
    public static final String pathImgTemporal = FragmentCrearLugar.directorioFoto + "/temp.jpg";

    File rutaImagenSent;
    File fileCamera;
    String pathImage = "";
    String urlImage = "";

    LocationManager myLocationManager; //Gestor del servicio de localización
    private boolean servicioActivo;
    private String provider;
    Location myLastLocation;

    HashMap<String, String> mapHeaders;
    String idUsuario;
    EditText edtNombreLugar, edtDescripcionLugar;
    TextInputLayout tilNombreLugar, tilDescripcionLugar;

    ProgressDialog progressDialog;

    public FragmentCrearLugar() {
        rutaImagenSent = new File(directorioFoto);

        if (!rutaImagenSent.mkdir()) rutaImagenSent.mkdirs();
        mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=utf-8");

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crear_lugar, container, false);
        context = inflater.getContext();
        idUsuario = LugaresConfiguracion.getIdUsuario(context);

        edtNombreLugar = (EditText) view.findViewById(R.id.id_edt_nombre_lugar);
        edtDescripcionLugar = (EditText) view.findViewById(R.id.id_edt_descripcion_lugar);

        tilNombreLugar = (TextInputLayout) view.findViewById(R.id.id_til_nombre_lugar);
        tilDescripcionLugar = (TextInputLayout) view.findViewById(R.id.id_til_descripcion_lugar);

        imgTomarfoto = (ImageView) view.findViewById(R.id.id_img_select_image);
        imgTomarfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        view.findViewById(R.id.id_btn_crear_lugar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

            }
        });

        return view;
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final CharSequence[] items = new CharSequence[2];

        items[0] = "Tomar foto";
        items[1] = "Seleccionar de galeria";


        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(
//                        getActivity(),
//                        "Seleccionaste:" + items[which],
//                        Toast.LENGTH_SHORT)
//                        .show();
                switch (which) {
                    case 1:
                        Intent intentSelectImage = new Intent();
                        intentSelectImage.setType("image/*");

                        intentSelectImage.setAction(Intent.ACTION_GET_CONTENT);

                        dialog.dismiss();
                        startActivityForResult(Intent.createChooser(intentSelectImage, "Seleccione imagen"), REQUEST_SELECT_IMAGE);
                        break;
                    case 0:
                        Intent intentCaptureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                        long time = System.currentTimeMillis();

                        File photo = new File(rutaImagenSent, "Img_" + time + ".jpg");

                        intentCaptureImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));

                        fileCamera = photo;
                        dialog.dismiss();
                        startActivityForResult(intentCaptureImage, REQUEST_CAPTURE_IMAGE);

                        break;
                }

            }
        });
        final AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void crearLugar() {


        tilNombreLugar.setError(edtNombreLugar.getText().toString().isEmpty() ? "Escriba un nombre" : null);
        tilDescripcionLugar.setError(edtDescripcionLugar.getText().toString().isEmpty() ? "Escriba una descripción" : null);

        if (tilDescripcionLugar.getError() == null && tilNombreLugar.getError() == null && !pathImage.isEmpty()) {

            progressDialog = new ProgressDialog(context);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Creando lugar");
            progressDialog.setCancelable(false);
            progressDialog.show();

            long time = System.currentTimeMillis();

            new UploadImg(pathImage, "Img_" + time + ".jpg").execute();

        } else {
            Toast.makeText(context, "No se permiten campos vacios", Toast.LENGTH_SHORT).show();
            edtNombreLugar.requestFocus();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {


            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    if (data.getData().getPath() != null) {

                        Log.e("PathImage", "Path: " + data.getData().getPath());


                        Cursor cursor = context.getContentResolver().query(data.getData(),
                                new String[]{
                                        MediaStore.Images.Media.DATA,
                                        MediaStore.Images.Media.MIME_TYPE,
                                        MediaStore.Images.Media.SIZE,
                                        MediaStore.Images.Media.ORIENTATION
                                },
                                null, null, null);
                        cursor.moveToFirst();

                        cursor.close();


                        pathImage = ImageFilePath.getPath(context, data.getData());
                        Bitmap bitmap = BitmapFactory.decodeFile(pathImage);
                        imgTomarfoto.setImageBitmap(ImageUtils.redimensionarImagenMaximo(bitmap, 640, 720));
                        Log.e("SelectImage", "path: " + pathImage);


                    }
                    break;


            }
        } else {
            if (requestCode == REQUEST_CAPTURE_IMAGE) {
                if (fileCamera.exists()) {
                    pathImage = fileCamera.getAbsolutePath();

                    Bitmap bitmap = BitmapFactory.decodeFile(pathImage);
                    imgTomarfoto.setImageBitmap(ImageUtils.redimensionarImagenMaximo(bitmap, 640, 720));
                    Log.e("fileCamera", "Existe: " + fileCamera.getAbsolutePath());
                } else {
                    Log.e("fileCamera", "NO Existe");
                }
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onLocationChanged(Location location) {
        myLastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void pararServicio() {
        servicioActivo = false;
        try {
            myLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("pararServicio", "Error: " + e.getMessage());
        }

    }

    public void getLocation() {

        servicioActivo = true;

        myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);

        provider = myLocationManager.getBestProvider(c, true);
        try {
            //Se activan las notificaciones de localización con los parámetros: proveedor, tiempo mínimo de actualización, distancia mínima, Locationlistener
            myLocationManager.requestLocationUpdates(provider, 10000, 1, this);
            //Obtenemos la última posición conocida dada por el proveedor

            myLastLocation = myLocationManager.getLastKnownLocation(provider);
            if (myLastLocation == null) {
                Toast.makeText(context, "No fue posible obtener la ubicación, por favor intente nuevamente", Toast.LENGTH_SHORT).show();
            } else {
                crearLugar();
            }

        } catch (SecurityException e) {
            Log.e("getLocation", "Error: " + e.getMessage());
        }


    }

    private class UploadImg extends AsyncTask<Void, Void, String> {

        String pathOrigen, nombre;


        public UploadImg(String pathOrigen, String nombre) {
            this.nombre = nombre;
            this.pathOrigen = pathOrigen;

        }

        @Override
        protected String doInBackground(Void... params) {

            return ImageUtils.serializarEnviarAdjunto(pathOrigen, nombre);


        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("result Asynctask", "Result: " + s);


            File tempPerfil = new File(pathImgTemporal);
            if (tempPerfil.exists()) {
                Log.e("perfilTemp", "Existe temp: " + tempPerfil.getAbsolutePath());
                tempPerfil.delete();
            }

            urlImage = s;

            HashMap<String, Object> mapCrearLugar = new HashMap<String, Object>();
            HashMap<String, String> mapParametrosCrearLugar = new HashMap<String, String>();

            mapParametrosCrearLugar.put(RestService.ParametroIdUsuarioLugar, idUsuario);
            mapParametrosCrearLugar.put(RestService.ParametroNombreLugar, edtNombreLugar.getText().toString() + ";;;" + urlImage);
            mapParametrosCrearLugar.put(RestService.ParametroDescripcionLugar, edtDescripcionLugar.getText().toString());
            mapParametrosCrearLugar.put(RestService.ParametroCoordenadaLugar, myLastLocation.getLatitude() + ";" + myLastLocation.getLongitude());


            mapCrearLugar.put(RestService.MetodoCrearLugar, mapParametrosCrearLugar);

            JSONObject jsonLoginRequest = new JSONObject(mapCrearLugar);
            Log.e("crearLugar", "Request: " + jsonLoginRequest.toString());

            new RestService(context).post(VolleySingleton.URI_SERVICE_PRUEBA, jsonLoginRequest.toString(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Log.e("crearLugar", "Result: " + response.toString());
                    Toast.makeText(context, "Lugrar creado con exito", Toast.LENGTH_SHORT).show();
                    clean();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(context, "Error de conexión, intente nuevamente", Toast.LENGTH_SHORT).show();
                    Log.e("crearLugar", "Error: " + error.getMessage());
                    clean();
                }
            }, mapHeaders);
        }

        private void clean() {

            imgTomarfoto.setImageResource(R.drawable.ic_select_image_lugares);
            edtNombreLugar.setText("");
            edtDescripcionLugar.setText("");
        }
    }

}
