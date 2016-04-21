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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

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
    String strlatitudParada=null;
    String strlongitudParada=null;
    GoogleMap googleMap;
    MapView mapView;
    float latitudRuta;
    float longitudRuta;
    float latitudParada;
    float longitudParada;
    private LatLng ltlng;
    private LatLng ltlngParada;
    int prescaler=0;

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDB.close();
        mapView.onDestroy();
        Log.d(TAG, "The onDestroy() event");
        stopService(new Intent(Main1.this, servicioDistancia.class));
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        mapView=(MapView)findViewById(R.id.mapa);
        mapView.onCreate(savedInstanceState);

//        latitudRuta=0;
//        longitudRuta=0;
//
//        ltlng=new LatLng(latitudRuta,longitudRuta);
//        googleMap=mapView.getMap();
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        googleMap.setMyLocationEnabled(true);
//        googleMap.addMarker(new MarkerOptions().position(ltlng).title("Ruta"));


        googleMap = mapView.getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);




        myDB = this.openOrCreateDatabase("coltrackSchool", MODE_PRIVATE, null);
        //myDB.execSQL("DROP TABLE IF EXISTS posicionRuta1;");//borramos tabla
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + "posicionRuta1"
                + " (ruta TEXT, distancia TEXT, correo TEXT, created_at TIMESTAMP, latitudRuta TEXT, longitudRuta TEXT, latitudParada TEXT, longitudParada TEXT);");

        prescaler=4;


        startService(new Intent(Main1.this, servicioDistancia.class));//Arrancamos servicio que pide medicion de distancia en servidor.

        //leerDB();
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {

                leerDB();
                if (!strlatitudRuta.equalsIgnoreCase("ND") && strlongitudRuta!="ND" && strlongitudParada!="ND" && strlatitudParada!="ND" && strlongitudRuta!=null && strlatitudRuta!=null && strlongitudParada!=null && strlatitudParada!=null ) {


                    googleMap.clear();

                    List<Marker> markers = new ArrayList<Marker>();

                    Log.d(TAG,"strlatitudRuta: "+strlatitudRuta);

                    //Marcador de la ruta
                    latitudRuta = Float.parseFloat(strlatitudRuta);
                    longitudRuta = Float.parseFloat(strlongitudRuta);
                    ltlng = new LatLng(latitudRuta, longitudRuta);




                    markers.add(googleMap.addMarker(new MarkerOptions().position(ltlng).title("Ruta")
                            .title("Ruta: " + ruta)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))));


                    //Marcador del paradero
                    latitudParada = Float.parseFloat(strlatitudParada);
                    longitudParada = Float.parseFloat(strlongitudParada);
                    ltlngParada=new LatLng(latitudParada,longitudParada);
                    markers.add(googleMap.addMarker(new MarkerOptions().position(ltlngParada).title("Parada")
                            .title("Parada")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.student))));




                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();

                    int padding = 100; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.moveCamera(cu);


                    prescaler++;
                    if (prescaler>5) {
                        prescaler=0;
                        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlngParada, 14));
                    }else {
                        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(ltlngParada));
                    }
                    //

                }
            }

            public void onFinish() {

                this.start();
            }
        }.start();


        //


    }

    void leerDB(){
        c=myDB.rawQuery("SELECT * FROM posicionRuta1", null);
        c.moveToFirst();
        Log.d(TAG,"Numero de filas: "+c.getCount());
        if (c!=null && c.getCount()>0){
            do {
                ruta=c.getString(c.getColumnIndex("ruta"));
                strDistancia=c.getString(c.getColumnIndex("distancia"));
                correo=c.getString(c.getColumnIndex("correo"));
                created_at=c.getString(c.getColumnIndex("created_at"));
                strlatitudRuta=c.getString(c.getColumnIndex("latitudRuta"));
                strlongitudRuta=c.getString(c.getColumnIndex("longitudRuta"));
                strlatitudParada=c.getString(c.getColumnIndex("latitudParada"));
                strlongitudParada=c.getString(c.getColumnIndex("longitudParada"));



            }while (c.moveToNext());
        }

    }



}
