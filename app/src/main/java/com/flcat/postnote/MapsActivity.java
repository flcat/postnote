package com.flcat.postnote;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // 글쓰기,수정 시 위치를 지정하기 위해 사용되는 뷰
    // 사용자 위치 수신기
    private LocationManager locationManager;
    private LocationListener locationListener;


    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    //Geocoder mGeocoder = new Geocoder(this);
    String adress = "";

    private GoogleMap mMap;

    // Acquire a reference to the system Location Manager
    //위도경도
    double lat;
    double lng;
    double latitude;
    double longitude;
    //최소 gps 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    int REQUEST_CODE_LOCATION=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        //사용자의 위치 수신을 위한 셋팅
        settingGPS();
        //사용자의 현재 위치
        Location userLocation = getMyLocation();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 카메라 좌표를 현위치로
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));

        // 구글지도에서 zoom level 은 1-23 까지 가능
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        mMap.animateCamera(zoom);
        //moveCamera 는 바로 변경하지만 animateCamera는 근거리에선 부드럽게 변함.

    }
    /*
     *
     * 사용자의 위치를 수신
     */
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);

        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
                /*
                try {
                    List<Address> mResultLocation = mGeocoder.getFromLocation(
                            currentLocation.getLatitude(),currentLocation.getLongitude(),1);
                    Log.d("주소 : "+mResultLocation.get(0),toString());
                } catch (IOException e1) {
                    e1.printStackTrace();

                }
                */
            }
        }
        return currentLocation;
    }
    /**
     * GPS 를 받기 위한 매니저와 리스너 설정
     */
    private void settingGPS() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
                // 카메라 좌표를 현위치로
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                //Toast.makeText(MapsActivity.this, latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                // marker 표시 마커의 위치 타이틀 짧은 설명 추가 가능
                final MarkerOptions marker = new MarkerOptions();

                marker.position(new LatLng(latitude, longitude));
                //         .title("서울역")
                //         .snippet("Seoul Station");
                mMap.addMarker(marker).showInfoWindow(); //마커추가 화면에 출력

                CircleOptions circle = new CircleOptions().center(marker.getPosition())
                        .radius(100) //반지름 미터
                        .strokeWidth(0f) //선너비
                        .fillColor(Color.parseColor("#880000ff")); //배경색

                mMap.addCircle(circle); //원 추가 화면에 출력

                // 마커 클릭 이벤트
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //마커 클릭시 호출되는 콜백 메서드
                        Toast.makeText(getApplicationContext(), marker.getPosition() + "가 클릭됨", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {

                        mMap.clear();
                        lat = latLng.latitude;
                        lng = latLng.longitude;
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                        marker.position(new LatLng(lat, lng));
                        //         .title("서울역")
                        //         .snippet("Seoul Station");
                        mMap.addMarker(marker).showInfoWindow(); //마커 추가 화면에 출력

                        Intent intent = new Intent(MapsActivity.this, WriteActivity.class);
                        /*
                        try {
                            List<Address> mResultLocation = mGeocoder.getFromLocation(
                                    lat,lng,1);
                            Log.d("주소 : "+mResultLocation.get(0),toString());
                            adress = mResultLocation.get(0).toString();

                        } catch (IOException e1) {
                            e1.printStackTrace();

                        }
                        */
                        intent.putExtra("slat",""+lat);
                        intent.putExtra("slng",""+lng);
                        //intent.putExtra("adress",mResultLocation.get(0),toString());
                        //Toast.makeText(getApplicationContext(), "MapA lat : "+lat+"\n lng :"+lng, Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK,intent);
                        WriteActivity.map_flag = 1;
                        finish();
                    }
                });
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }
    /**
     * GPS 권한 응답에 따른 처리
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    boolean canReadLocation = false;
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                Location userLocation = getMyLocation();
                if( userLocation != null ) {

                    // todo 사용자의 현재 위치 구하기
                    latitude = userLocation.getLatitude();
                    longitude = userLocation.getLongitude();
                    /*
                    try {
                        List<Address> mResultLocation = mGeocoder.getFromLocation(
                               userLocation.getLatitude(),userLocation.getLongitude(),1);
                        Log.d("주소 : "+mResultLocation.get(0),toString());
                    } catch (IOException e1) {
                        e1.printStackTrace();

                    }
                    */
                }
                canReadLocation = true;
            } else {
                // Permission was denied or request was cancelled
                canReadLocation = false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
