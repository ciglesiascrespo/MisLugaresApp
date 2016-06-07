package com.example.ciglesias_pc.pruebasainet.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.NetworkOnMainThreadException;
import android.util.Base64;
import android.util.Log;

import com.example.ciglesias_pc.pruebasainet.fragments.FragmentCrearLugar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ciglesias-pc on 06/06/2016.
 */
public class ImageUtils {

    private static final String NAMESPACE = "http://tempuri.org/wsOnTRACK/";
    private static final String urlWebService = "http://69.175.75.147/wsandrossb/wsontrack.asmx";

    public static Bitmap redimensionarImagenMaximo(Bitmap mBitmap, float newWidth, float newHeigth) {

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth;
        float scaleHeight;

        if (height > newHeigth || width > newWidth) {
            Log.e("redimensionarImagen", "redimensionado imagen");
            scaleWidth = ((float) newWidth) / width;
            scaleHeight = ((float) newHeigth) / height;
            Matrix matrix = new Matrix();

            matrix.postScale(scaleWidth, scaleHeight);

            return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
        } else {
            Log.e("redimensionarImagen", "imagen original");
            return mBitmap;
        }

    }


    public static Bitmap decodeImage(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String encodeImage(String path) {
        String encodedImage;
        Bitmap bitmap = redimensionarImagenMaximo(BitmapFactory.decodeFile(path), 640, 720);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, baos);
        byte[] byteArrayImage = baos.toByteArray();

        encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        return encodedImage;
    }


    public static String guardarImagenPerfilTemporal(String pathOrigen) {
        String pathDestino = FragmentCrearLugar.pathImgTemporal;

        try {
            Bitmap myBitmap = BitmapFactory.decodeFile(pathOrigen);

            Log.e("guardarImagenPerfil", "original: " + pathOrigen);
            if (myBitmap != null) {
                Bitmap imagen = redimensionarImagenMaximo(myBitmap, 640, 720);
                FileOutputStream fos = null;

                fos = new FileOutputStream(pathDestino);
                imagen.compress(Bitmap.CompressFormat.PNG, 10, fos);
                fos.flush();
            } else {
                Log.e("ConfiguracionActivity", "Error: ");
            }
        } catch (FileNotFoundException ex) {

            ex.printStackTrace();
        } catch (IOException ex) {
            Log.e("ConfiguracionActivity", "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return pathDestino;
    }

    public static String serializarEnviarAdjunto(String rutaOrigen,
                                           String nomFileDestino) {
        SoapPrimitive resultString;
        try {
            byte[] raw = null;

            Log.e("Enviar Imagen", "enviar img: " + rutaOrigen);

            File file_rutaOrigen = new File(guardarImagenPerfilTemporal(rutaOrigen));

            byte[] a = org.apache.commons.io.FileUtils
                    .readFileToByteArray(file_rutaOrigen);

            byte[] b2 = new byte[1];

            b2[0] = a[a.length - 1];
            byte[] c2 = new byte[a.length + b2.length];
            System.arraycopy(a, 0, c2, 0, a.length);
            System.arraycopy(b2, 0, c2, a.length, b2.length);
            raw = c2;


            // Conexion al Webservice
            MarshalBase64 b = new MarshalBase64();
            SoapObject request = new SoapObject(NAMESPACE, "recibirImagenPerfil");
            request.addProperty("MyData", raw);
            request.addProperty("m_nomFile", nomFileDestino);

            SoapSerializationEnvelope soapEnv = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            soapEnv.dotNet = true;
            soapEnv.bodyOut = request;
            soapEnv.implicitTypes = true;
            soapEnv.encodingStyle = "utf-8";
            soapEnv.encodingStyle = SoapSerializationEnvelope.XSD;
            soapEnv.setOutputSoapObject(request);
            b.register(soapEnv);
            HttpTransportSE aht = new HttpTransportSE(urlWebService);
            aht.call("http://tempuri.org/wsOnTRACK/recibirImagenPerfil", soapEnv);
            aht.debug = true;
            resultString = (SoapPrimitive) soapEnv.getResponse();

            Log.e("ConfiguracionActivity", "Result: " + resultString);

            System.gc();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "ERROR";
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return "ERROR";
        } catch (NetworkOnMainThreadException e) {
            Log.e("Serializar", "Error: " + e.getCause());
            e.printStackTrace();
            return "ERROR";
        }
        return "" + resultString;
    }

}
