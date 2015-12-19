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


    //GPS 관련 변수
    private GoogleMap map;
    Marker myLocation;
    LocationManager locationManager;
    String address = null;

    //현재 시간을 알아오기 위한 변수
    String time;
    TextView clock;

    //메모장 버튼
    Button bt_log_write;

    //Thread 종료를 위한 변수
    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //시간을 보기 위해 TextView
        clock = (TextView) findViewById(R.id.clock_view);

        //GPS를 위한 Map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //GPS LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //메모장으로 가는 버튼
        bt_log_write = (Button) findViewById(R.id.bt_log_view);

    }

    //버튼 이벤트 처리는 onClick method를 활용하였다.
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_log_view:
                //메모장으로 가기위한 Intent. GPS로 찾은 현재 주소를 다음 Activity로 넘긴다.
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

        //Thread 실행을 위해 true
        isRun = true;

        //위치 서비스가 꺼져있을경우 다시 켜도록 Check
        checkGpsService();

        //시간을 실시간으로 표시하기위한 Thread
        Thread clockThread = new Thread(new Runnable() {
            public void run() {
                while (isRun) {
                    try {
                        //handler에 0을 보낸다.
                        handler.sendEmptyMessage(0);
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //clockThread 시작
        clockThread.start();

        //GPS Thread. GPS LocationManager를 실행시킨다.
        Thread gpsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //handler에 1을 보낸다.
                    handler.sendEmptyMessage(1);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //GPS Thread 시작
        gpsThread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //clockThread를 종료
        threadStop();
    }

    //Thread를 위한 handler
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

    //clockThread 실시간으로 시간을 TextView에 보여준다.
    private void clockThread() {
        //시간을 얻기위한 DataFormat
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdfNow.format(new Date(System.currentTimeMillis()));
        //TextView에 보여준다.
        clock.setText(time);
    }

    //위도와 경도로 부터 현재의 주소를 알려주는 method
    public String getAddress(double lat, double lng) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> list = null;

        //Input으로 위도 경도를 넣으면 주소를 List에 넣는다.
        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }

        //주소를 adress라는 String형식에 담아 return한다.
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

    //위도와 경도를 바탕으로 지도에 Marker해주는 Method.
    public void setMapPosition(double lat, double lng) {
        LatLng position = new LatLng(lat, lng);

        //Marker가 없을 경우 Marker해준다.
        if (myLocation == null) {
            MarkerOptions options = new MarkerOptions();
            options.position(position);
            //주소를 저장한다.
            options.title(getAddress(lat, lng));
            myLocation = map.addMarker(options);
        } else {
            //Marker가 있을경우 Marker를 업데이트 해준다.
            myLocation.setPosition(position);
            //주소를 저장한다.
            myLocation.setTitle(getAddress(lat, lng));
        }

        //Map을 적당한 크기로 볼 수 있게 줌을 해준다.
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 17);
        map.animateCamera(camera);
    }

    //GPS기능이 켜져있는지 체크해주는 Method
    private boolean checkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            //DIalog를 띄워 위치기능을 설정하게 한다.
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Intent를 사용해 설정창을 열어준다.
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

    //GPS LocationManager로 나의 위치를 실시간으로 잡아준다.
    public void gpsThread() {

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                //위치가 변하면 setMapPosition Method를 실행
                setMapPosition(lat, lng);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {


            }

            public void onProviderDisabled(String provider) {

            }
        };

        //0초, 0m마다 업데이트를 실시한다.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    //Thread를 종료하기위한 method
    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
