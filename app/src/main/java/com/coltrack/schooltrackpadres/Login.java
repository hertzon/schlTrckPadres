package com.coltrack.schooltrackpadres;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.pushy.sdk.Pushy;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


public class Login extends AppCompatActivity {
    String TAG="Debug";
    EditText editTextusuario;
    EditText editTextclave;
    Button btnIngresar;
    public static String strUsuario;
    public static String strClave;
    private ProgressBar mRegistrationProgressBar;
    JSONObject jsonObject;
    String registrationId=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (android.os.Build.VERSION.SDK_INT > 9)
//        {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        long interval = 15000; // Every 15 seconds
        Pushy.setHeartbeatInterval(interval, this);
        Log.i(TAG, "Escuchando pushy");
        Pushy.listen(this);
        setContentView(R.layout.activity_login);

        new registerForPushNotificationsAsync().execute();

        editTextusuario=(EditText)findViewById(R.id.editTextUsuario);
        editTextclave=(EditText)findViewById(R.id.editTextPassword);
        btnIngresar=(Button)findViewById(R.id.buttonIngresar);

        new GPSTracker(Login.this);
        double latitude  = GPSTracker.latitude; // latitude
        double longitude = GPSTracker.latitude; // latitude

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strUsuario=editTextusuario.getText().toString();
                strClave=editTextclave.getText().toString();
                if (registrationId!=null){
                    //si los datos intruducidos son correctos enviamos datos a servidor incluido registrationId
                    BackgroundTask bkgTask = new BackgroundTask();
                    bkgTask.execute();

                }else {
                    Toast.makeText(Login.this, "No se ha registrado en pushy", Toast.LENGTH_SHORT).show();
                }

            }
        });






    }
    private class registerForPushNotificationsAsync extends AsyncTask<Void, Void, Exception>
    {
        protected Exception doInBackground(Void... params)
        {
            try
            {
                // Acquire a unique registration ID for this device
                registrationId = Pushy.register(getApplicationContext());
                Log.i(TAG, "Registration ID: " +registrationId);

                // Send the registration ID to your backend server and store it for later
                sendRegistrationIdToBackendServer(registrationId);
            }
            catch( Exception exc )
            {
                // Return exc to onPostExecute
                return exc;
            }

            // We're good
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc)
        {
            // Failed?
            if ( exc != null )
            {
                // Show error as toast message
                Toast.makeText(getApplicationContext(), exc.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            // Succeeded, do something to alert the user
        }

        // Example implementation
        void sendRegistrationIdToBackendServer(String registrationId) throws Exception
        {
            // The URL to the function in your backend API that stores registration IDs
            URL sendRegIdRequest = new URL("https://{YOUR_API_HOSTNAME}/register/device?registration_id=" + registrationId);

            // Send the registration ID by executing the GET request
            sendRegIdRequest.openConnection();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(getApplication(),"onResume",Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(getApplication(),"onPause",Toast.LENGTH_SHORT).show();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(Login.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(TAG, "preexecute");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        Toast.makeText(Login.this, "Usuario y/o contraseña invalidos!!!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            Log.d(TAG, "doing");
            jsonObject = new JSONObject();
            //SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = s.format(new Date());
            Log.i(TAG, "currentTimeStamp: " + currentTimeStamp);
            try {
                jsonObject.put("usuario", strUsuario);
                jsonObject.put("password", strClave);
                jsonObject.put("token", registrationId);
                jsonObject.put("timestamp", currentTimeStamp);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.38.31/phpDir/loginGCM.php", nameValuePairs);
                if (response.equals("0")) {
                    //Toast.makeText(MainActivity.this,"Usuario y/o contraseña errados!",Toast.LENGTH_SHORT).show();
                    status = false;
                }
                if (response.equals("1")) {
                    //Toast.makeText(MainActivity.this,"Bienvenido!",Toast.LENGTH_SHORT).show();
                    status = true;

                    //token=RegistrationIntentService.token;
                    if (!registrationId.equals(null)) {
                        Intent imain1 = new Intent(Login.this, Main1.class);
                        imain1.putExtra("usuario", strUsuario);
                        imain1.putExtra("clave", strClave);
                        imain1.putExtra("token", registrationId);
                        startActivity(imain1);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return status;
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
                        String estadoLogin = object.getString("action");

                        Log.d(TAG, "Estado Login:" + estadoLogin);
                        response = estadoLogin;
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


        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return answer;
        }
    }
}
