package com.flcat.postnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsClusterActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    // 구글지도 상에 클러스터화 시킨 글을 표시하기위한 뷰
    Marker selectedMarker;
    View marker_root_view;
    TextView tv_marker;
    private GoogleMap mMap;
    @Override protected void onCreate(Bundle savedInstanceState)
    { super.onCreate(savedInstanceState); setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager() .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.537523, 126.96558), 5));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        setCustomMarkerView();
        //getSampleMarkerItems();
        getMarkerItem();

    }
    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }
    private void getMarkerItem(){
        // DB에서 title,lat,lng 을 읽어와 갯수만큼 마커를 생성하고 값을 입력
        String title = "", slat, slng;
        Double lat = null,lng = null;
        ArrayList<PositionItem> list = new ArrayList();
        try{
            JSONObject jsonObject = new JSONObject(TodoActivity.notelist);
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            //아이템 갯수 결정
            for (int i = 0; i < jsonArray.length() ;i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                title = object.getString("title");
                slat = object.getString("lat");
                slng = object.getString("lng");
                lat = Double.parseDouble(slat);
                lng = Double.parseDouble(slng);
                System.out.print("이름"+title);
                System.out.print("좌표"+lat+"/"+lng);
                /*
                //값을 가져오는데는 문제 없음
                Log.e("이름",title);
                Log.e("좌표",lat+"/"+lng);
                */
                list.add(new PositionItem(lat,lng,title));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        for (PositionItem markerItem : list) {
            addMarker(markerItem, false);
        }
    }
    private Marker addMarker(PositionItem markerItem, boolean isSelectedMarker) {
        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLng());
        String title = markerItem.getTitle();
        //String formatted = NumberFormat.getCurrencyInstance().format((title));
        tv_marker.setText(title);
        if (isSelectedMarker) {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone_blue);
            tv_marker.setTextColor(Color.WHITE);
        } else {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone);
            tv_marker.setTextColor(Color.BLACK);
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
        return mMap.addMarker(markerOptions);
    }
    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
    ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels); view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
    view.buildDrawingCache();
    Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap); view.draw(canvas);
    return bitmap;
    }
    private Marker addMarker(Marker marker, boolean isSelectedMarker) {
        double lat = marker.getPosition().latitude;
        double lon = marker.getPosition().longitude;
     String title = marker.getTitle();
     PositionItem temp = new PositionItem(lat, lon, title);
     return addMarker(temp, isSelectedMarker);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        changeSelectedMarker(marker);
        return true;
    }
    private void changeSelectedMarker(Marker marker) { // 선택했던 마커 되돌리기
        if (selectedMarker != null) {
            addMarker(selectedMarker, false);
            selectedMarker.remove();
        }
        // 선택한 마커 표시
        if (marker != null) {
            selectedMarker = addMarker(marker, true);
            marker.remove();
        }
    }
        @Override
        public void onMapClick (LatLng latLng){
            changeSelectedMarker(null);
        }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
