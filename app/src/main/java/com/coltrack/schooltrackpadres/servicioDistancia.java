package com.coltrack.schooltrackpadres;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class servicioDistancia extends Service {
    String TAG="Debug";
    MyTask myTask;
    JSONObject jsonObject;
    String strDistancia;
    Double distancia;
    SQLiteDatabase myDB;
    String correo=null;
    String ruta=null;
    String latitudRuta=null;
    String longitudRuta=null;


    public servicioDistancia() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Servicio creado!", Toast.LENGTH_SHORT).show();
        myDB = this.openOrCreateDatabase("coltrackSchool", MODE_PRIVATE, null);
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + "posicionRuta"
                + " (ruta TEXT, distancia TEXT, correo TEXT, created_at TIMESTAMP, latitudRuta TEXT, longitudRuta TEXT);");



        myTask = new MyTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myTask.execute();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy Servicio");
        myTask.cancel(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class MyTask extends AsyncTask<String, String, String> {

        private DateFormat dateFormat;
        private String date;
        private boolean cent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            cent = true;
        }

        @Override
        protected String doInBackground(String... params) {
            while (cent){
                date = dateFormat.format(new Date());
                jsonObject = new JSONObject();
                correo=Login.strUsuario;
                try {
                    jsonObject.put("correoAcudiente", correo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "Correo a enviar: " + correo);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response=null;
                response = makePOSTRequest("http://107.170.38.31/phpDir/mideDistancia.php", nameValuePairs );
                try {
                    publishProgress(date);
                    // Stop 5s
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(getApplicationContext(), "Hora actual: " + values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cent = false;
            myDB.close();
        }

    }
    public String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
        String response = "";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = null;
                Log.d(TAG, "Response:" + jsonResult);
                try {
                    object = new JSONObject(jsonResult);
                    strDistancia  = object.getString("respuesta");
                    latitudRuta=object.getString("latitudRuta");
                    longitudRuta=object.getString("longitudRuta");

                    distancia=Double.parseDouble(strDistancia);
                    //Guardamos distancia y pos de la ruta en la DB
                    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTimeStamp = s.format(new Date());
                    myDB.execSQL("INSERT INTO "
                            + "posicionRuta"
                            + " (ruta, distancia, correo, created_at, latitudRuta, longitudRuta)"
                            + " VALUES ("+"'"+ ruta+"'" + ", "+"'"+strDistancia+"'"+", "+"'"+correo+"'"+", "+"'"+currentTimeStamp+"'"+", "+"'"+latitudRuta+"'"+", "+"'"+longitudRuta+"'"+");");



                    if (distancia<0.5){

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND);

                        mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
                        mBuilder.setContentTitle("Ruta Escolar Cerca");
                        mBuilder.setContentText("Atencion la ruta escolar se encuentra a: "+strDistancia);
                        mBuilder.setTicker("ticker");

                        Intent inotificacion=new Intent(this,Main1.class);
                        PendingIntent intentpendiente=PendingIntent.getActivity(this,0,inotificacion,0);
                        mBuilder.setContentIntent(intentpendiente);
                        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancelAll();
                        nm.notify(10, mBuilder.build());

                    }




                    Log.d(TAG, "Distancia a ruta:" + distancia);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return response;
    }
    private StringBuilder inputStreamToString(InputStream is){
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try
        {
            while ((rLine = rd.readLine()) != null)
            {
                answer.append(rLine);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return answer;
    }

}
