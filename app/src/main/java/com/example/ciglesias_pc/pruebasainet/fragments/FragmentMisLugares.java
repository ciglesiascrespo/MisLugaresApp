package com.example.ciglesias_pc.pruebasainet.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.ciglesias_pc.pruebasainet.Lugares.Lugar;
import com.example.ciglesias_pc.pruebasainet.R;
import com.example.ciglesias_pc.pruebasainet.Utils.ImageUtils;
import com.example.ciglesias_pc.pruebasainet.service.RestService;
import com.example.ciglesias_pc.pruebasainet.service.VolleySingleton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ciglesias-pc on 06/06/2016.
 */
public class FragmentMisLugares extends Fragment implements OnMapReadyCallback {
    GoogleMap Map;
    private MapView mapView;
    Context context;
    HashMap<String, String> mapHeaders;

    ArrayList<Lugar> arrayListLugares = new ArrayList<>();

    ListView listViewLugares;
    Switch switchMap;
    LinearLayout lyLugaresContainer;

    AdapterListLugares myAdapterListLugares;

    public FragmentMisLugares() {
        mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=utf-8");
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_lugares, container, false);
        context = getContext();
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        lyLugaresContainer = (LinearLayout) view.findViewById(R.id.id_list_lugares_container);
        listViewLugares = (ListView) view.findViewById(R.id.id_list_lugares);

        myAdapterListLugares = new AdapterListLugares(context);

        listViewLugares.setAdapter(myAdapterListLugares);
        switchMap = (Switch) view.findViewById(R.id.id_switch_map);
        switchMap.setChecked(true);
        switchMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showMap = switchMap.isChecked();
                lyLugaresContainer.setVisibility(showMap ? View.GONE : View.VISIBLE);
                mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
            }
        });
        cargarLugares();
        return view;
    }

    ProgressDialog progressDialog;

    private void cargarLugares() {

        progressDialog = new ProgressDialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setMessage("Cargando mis lugares");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new RestService(context).post(VolleySingleton.URI_SERVICE_PRUEBA, RestService.MetodoCargarLugares, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Log.e("cargarlugares", "Result: " + response.toString());
                procesarLugares(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(context, "Error de conexión, intente nuevamente", Toast.LENGTH_SHORT).show();
                Log.e("cargarLugares", "Error: " + error.getMessage());
            }
        }, mapHeaders);
    }

    private void procesarLugares(JSONObject jsonObject) {
        arrayListLugares = new ArrayList<>();
        try {
            JSONArray listaSitiosJson = jsonObject.getJSONArray("ListaSitios");

            if (listaSitiosJson != null) {
                if (listaSitiosJson.length() > 0) {
                    for (int i = 0; i < listaSitiosJson.length(); i++) {
                        JSONObject jsonSitio = listaSitiosJson.getJSONObject(i);

                        String nombreYfoto = jsonSitio.getString("Nombre");
                        String nombre, urlImage = "";
                        String nombreyfotoArray[] = nombreYfoto.split(";;;");

                        nombre = nombreyfotoArray[0];

                        if (nombreyfotoArray.length > 1) {
                            urlImage = nombreyfotoArray[1];
                        }
                        String descripcion = jsonSitio.getString("Descripcion");
                        String coordenadas = jsonSitio.getString("Coordenadas");

                        String coordenadasArray[] = coordenadas.split(";");

                        Double latitud = 0.0;
                        Double longitud = 0.0;
                        if (coordenadasArray.length > 1) {
                            latitud = Double.valueOf(coordenadas.split(";")[0]);
                            longitud = Double.valueOf(coordenadas.split(";")[1]);
                            arrayListLugares.add(new Lugar(nombre, descripcion, latitud, longitud, urlImage));
                        }

                    }
                    addMarkers();
                    myAdapterListLugares.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "No hay lugares que mostrar", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("procesarLugares", "Error: " + e.getMessage());
        }
    }

    private void addMarkers() {
        for (int i = 0; i < arrayListLugares.size(); i++) {

            Lugar lugar = arrayListLugares.get(i);


            LatLng latLng = new LatLng(lugar.getLatitud(), lugar.getLongitud());
            Map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(lugar.getNombre())
                    .snippet(lugar.getDescripcion()));
            if (i == 0) {
                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(latLng)
                        .zoom(10)
                        .build();

                Map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("onMapReady", "ok");
        Map = googleMap;

    }


    private class AdapterListLugares extends BaseAdapter {

        Context mContext;
        LayoutInflater inflater;

        public AdapterListLugares(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return arrayListLugares.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayListLugares.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;
            Lugar lugar = arrayListLugares.get(position);

            if (view == null) {
                view = inflater.inflate(R.layout.item_list_lugares, null);
                viewHolder = new ViewHolder();
//
                viewHolder.setView("txt_nombre_lugar", view.findViewById(R.id.id_txt_nombre_lugar));
                viewHolder.setView("txt_latitud_lugar", view.findViewById(R.id.id_txt_latitud_lugar));
                viewHolder.setView("txt_longitud_lugar", view.findViewById(R.id.id_txt_longitud_lugar));
                viewHolder.setView("img_lugar", view.findViewById(R.id.id_img_lugar));
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.getView("txt_nombre_lugar", TextView.class).setText(lugar.getNombre());
            viewHolder.getView("txt_latitud_lugar", TextView.class).setText(String.valueOf(lugar.getLatitud()));
            viewHolder.getView("txt_longitud_lugar", TextView.class).setText(String.valueOf(lugar.getLongitud()));
            displayUrlImage(viewHolder.getView("img_lugar", ImageView.class), lugar.getImageEncoded());


            return view;

        }

        private class ViewHolder {
            private HashMap<String, View> holder = new HashMap<String, View>();

            public void setView(String k, View v) {
                holder.put(k, v);
            }

            public View getView(String k) {
                return holder.get(k);
            }

            public <T> T getView(String k, Class<T> type) {
                return type.cast(getView(k));
            }
        }
    }

    private static void displayUrlImage(ImageView imageView, String url) {
        UrlDownloadAsyncTask.display(url, imageView);
    }

    private static class UrlDownloadAsyncTask extends AsyncTask<Void, Void, Object> {
        private static LRUCache cache = new LRUCache((int) (Runtime.getRuntime().maxMemory() / 16)); // 1/16th of the maximum memory.
        private final UrlDownloadAsyncTaskHandler handler;
        private String url;


        public static void display(String url, final ImageView imageView) {
            UrlDownloadAsyncTask task = null;

            if (imageView.getTag() != null && imageView.getTag() instanceof UrlDownloadAsyncTask) {
                try {
                    task = (UrlDownloadAsyncTask) imageView.getTag();
                    task.cancel(true);
                } catch (Exception e) {
                }

                imageView.setTag(null);
            }

            task = new UrlDownloadAsyncTask(url, new UrlDownloadAsyncTaskHandler() {
                @Override
                public void onPreExecute() {
                    imageView.setImageResource(R.drawable.logo_sainet_menu);
                }

                @Override
                public Object doInBackground(File file) {
                    if (file == null) {
                        return null;
                    }

                    Bitmap bm = null;
                    try {
                        int targetHeight = 256;
                        int targetWidth = 256;

                        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                        bin.mark(bin.available());

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(bin, null, options);

                        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth);

                        if (options.outHeight * options.outWidth >= targetHeight * targetWidth) {
                            double sampleSize = scaleByHeight
                                    ? options.outHeight / targetHeight
                                    : options.outWidth / targetWidth;
                            options.inSampleSize = (int) Math.pow(2d, Math.floor(Math.log(sampleSize) / Math.log(2d)));
                        }

                        try {
                            bin.reset();
                        } catch (IOException e) {
                            bin = new BufferedInputStream(new FileInputStream(file));
                        }

                        // Do the actual decoding
                        options.inJustDecodeBounds = false;
                        bm = BitmapFactory.decodeStream(bin, null, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return bm;
                }

                @Override
                public void onPostExecute(Object object, UrlDownloadAsyncTask task) {
                    if (object != null && object instanceof Bitmap && imageView.getTag() == task) {
                        imageView.setImageBitmap((Bitmap) object);
                    }
                }
            });

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }

            imageView.setTag(task);
        }

        public UrlDownloadAsyncTask(String url, UrlDownloadAsyncTaskHandler handler) {
            this.handler = handler;
            this.url = url;
        }

        public interface UrlDownloadAsyncTaskHandler {
            public void onPreExecute();

            public Object doInBackground(File file);

            public void onPostExecute(Object object, UrlDownloadAsyncTask task);
        }

        @Override
        protected void onPreExecute() {
            if (handler != null) {
                handler.onPreExecute();
            }
        }

        protected Object doInBackground(Void... args) {
            File outFile = null;
            try {
                if (cache.get(url) != null && new File(cache.get(url)).exists()) { // Cache Hit
                    outFile = new File(cache.get(url));
                } else { // Cache Miss, Downloading a file from the url.
                    outFile = File.createTempFile("lugar-download", ".tmp");
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));

                    InputStream input = new BufferedInputStream(new URL(url).openStream());
                    byte[] buf = new byte[1024 * 100];
                    int read = 0;
                    while ((read = input.read(buf, 0, buf.length)) >= 0) {
                        outputStream.write(buf, 0, read);
                    }

                    outputStream.flush();
                    outputStream.close();
                    cache.put(url, outFile.getAbsolutePath());
                }


            } catch (IOException e) {
                e.printStackTrace();

                if (outFile != null) {
                    outFile.delete();
                }

                outFile = null;
            }


            if (handler != null) {
                return handler.doInBackground(outFile);
            }

            return outFile;
        }

        protected void onPostExecute(Object result) {
            if (handler != null) {
                handler.onPostExecute(result, this);
            }
        }

        private static class LRUCache {
            private final int maxSize;
            private int totalSize;
            private ConcurrentLinkedQueue<String> queue;
            private ConcurrentHashMap<String, String> map;

            public LRUCache(final int maxSize) {
                this.maxSize = maxSize;
                this.queue = new ConcurrentLinkedQueue<String>();
                this.map = new ConcurrentHashMap<String, String>();
            }

            public String get(final String key) {
                if (map.containsKey(key)) {
                    queue.remove(key);
                    queue.add(key);
                }

                return map.get(key);
            }

            public synchronized void put(final String key, final String value) {
                if (key == null || value == null) {
                    throw new NullPointerException();
                }

                if (map.containsKey(key)) {
                    queue.remove(key);
                }

                queue.add(key);
                map.put(key, value);
                totalSize = totalSize + getSize(value);

                while (totalSize >= maxSize) {
                    String expiredKey = queue.poll();
                    if (expiredKey != null) {
                        totalSize = totalSize - getSize(map.remove(expiredKey));
                    }
                }
            }

            private int getSize(String value) {
                return value.length();
            }
        }
    }
}
