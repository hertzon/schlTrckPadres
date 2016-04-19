package com.coltrack.schooltrackpadres;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class Main1 extends AppCompatActivity {
    SQLiteDatabase myDB;
    Cursor c;
    String TAG="Debug";
    String ruta=null;
    String strDistancia=null;
    String correo=null;
    String created_at=null;
    String strlatitudRuta=null;
    String strlongitudRuta=null;
    EditText editTextDistancia;
    EditText editTextLatitud;
    EditText editTextLongitud;
    EditText editTextUltimoReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        editTextDistancia=(EditText)findViewById(R.id.editTextDistancia);
        editTextLatitud=(EditText)findViewById(R.id.editTextLatitud);
        editTextLongitud=(EditText)findViewById(R.id.editTextLongitud);
        editTextUltimoReporte=(EditText)findViewById(R.id.editTextUltimoReporte);

        myDB = this.openOrCreateDatabase("coltrackSchool", MODE_PRIVATE, null);
        myDB.execSQL("DROP TABLE IF EXISTS posicionRuta");//borramos tabla
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + "posicionRuta"
                + " (ruta TEXT, distancia TEXT, correo TEXT, created_at TIMESTAMP, latitudRuta TEXT, longitudRuta TEXT);");



        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                //editTextDistancia.setText("seconds remaining: " + millisUntilFinished / 1000);
                editTextDistancia.setText(strDistancia+ " Km");
                editTextLatitud.setText(strlatitudRuta);
                editTextLongitud.setText(strlongitudRuta);
                editTextUltimoReporte.setText(created_at);
                leerDB();
            }

            public void onFinish() {
                editTextDistancia.setText("done!");
                this.start();
            }
        }.start();
        editTextDistancia.setText("no");

        //

        startService(new Intent(Main1.this,servicioDistancia.class));//Arrancamos servicio que pide medicion de distancia en servidor.
    }

    void leerDB(){
        c=myDB.rawQuery("SELECT * FROM posicionRuta", null);
        c.moveToFirst();
        if (c!=null && c.getCount()>0){
            do {
                ruta=c.getString(c.getColumnIndex("ruta"));
                strDistancia=c.getString(c.getColumnIndex("distancia"));
                correo=c.getString(c.getColumnIndex("correo"));
                created_at=c.getString(c.getColumnIndex("created_at"));
                strlatitudRuta=c.getString(c.getColumnIndex("latitudRuta"));
                strlongitudRuta=c.getString(c.getColumnIndex("longitudRuta"));



            }while (c.moveToNext());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myDB.close();
        Log.d(TAG, "The onDestroy() event");
    }

}
