package kr.kookmin.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private GoogleMap map;
    Marker myLocation;
    LocationManager locationManager;
    String address = null;

    String time;
    TextView clock;

    Button bt_log_wirte;

    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clock = (TextView) findViewById(R.id.clock_view);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bt_log_wirte = (Button) findViewById(R.id.bt_log_view);


    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_log_view:
                Intent intent = new Intent(this, MemoListActivity.class);
                intent.putExtra("address", address);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        isRun = true;
        checkGpsService();

        Thread clockThread = new Thread(new Runnable() {
            public void run() {
                while (isRun) {
                    try {
                        handler.sendEmptyMessage(0);
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        clockThread.start();

        Thread gpsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.sendEmptyMessage(1);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        gpsThread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        threadStop();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    clockThread();
                    break;
                case 1:
                    gpsThread();
                    break;

            }

        }
    };


    private void clockThread() {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdfNow.format(new Date(System.currentTimeMillis()));
        clock.setText(time);
    }

    public String getAddress(double lat, double lng) {


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }

        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getCountryName() + " "
                    + addr.getAdminArea() + " "
                    + addr.getLocality() + " "
                    + addr.getThoroughfare() + " "
                    + addr.getFeatureName();
        }

        return address;
    }

    public void setMapPosition(double lat, double lng) {
        LatLng position = new LatLng(lat, lng);

        if (myLocation == null) {
            MarkerOptions options = new MarkerOptions();
            options.position(position);
            options.title(getAddress(lat, lng));
            myLocation = map.addMarker(options);
        } else {
            myLocation.setPosition(position);
            myLocation.setTitle(getAddress(lat, lng));
        }

        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 17);
        map.animateCamera(camera);
    }

    private boolean checkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }

    public void gpsThread() {

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                setMapPosition(lat, lng);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {


            }

            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
