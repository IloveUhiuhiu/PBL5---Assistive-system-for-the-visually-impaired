package com.example.gpsapp.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.gpsapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapPage extends FragmentActivity implements OnMapReadyCallback {

    private final String API_KEY = "5b3ce3597851110001cf6248cac1d0109aaa4ad29d12054b5b987604";
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private double selectedLat, selectedLng;
    private boolean isDeviceSelected = false;
    private FloatingActionButton btnHome;
    private String deviceId;
    List<LatLng> waypoints = new ArrayList<>();
    private Marker deviceMarker;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.gpsapp.R.layout.activity_map_page);

        btnHome = findViewById(com.example.gpsapp.R.id.btn_home);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapPage.this, HomePage.class);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            deviceId = intent.getStringExtra("deviceId");
            Log.d("DEBUG", "Device ID: " + deviceId);
            isDeviceSelected = true   ;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("locations");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (isDeviceSelected) {
            FirebaseDatabase.getInstance()
                            .getReference("devices")
                            .child(deviceId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Double lat = snapshot.child("latitude").getValue(Double.class);
                                        Double lng = snapshot.child("longitude").getValue(Double.class);
                                        if (lat != null && lng != null) {
                                            LatLng deviceLocation = new LatLng(lat, lng);

                                            if (deviceMarker == null) {
                                                deviceMarker = mMap.addMarker(new MarkerOptions()
                                                        .position(deviceLocation)
                                                        .title("Device: " + deviceId)
                                                        .icon(createGreenDotIcon()));
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, 15));
                                            } else {
                                                deviceMarker.setPosition(deviceLocation);
                                                mMap.moveCamera(CameraUpdateFactory.newLatLng(deviceLocation));
                                            }
                                        } else {
                                            Log.w("FirebaseWarning", "Tọa độ không hợp lệ cho deviceId: " + deviceId);
                                        }
                                    } else {
                                        Log.e("FirebaseError", "Device not found in Firebase");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("MapError", "Lỗi truy vấn Firebase: " + error.getMessage());
                                }
                            });
        } else {
//            // Nếu không có thiết bị nào được chọn, tải danh sách vị trí từ Firebase
//            databaseRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    mMap.clear();
//                    List<LatLng> pathPoints = new ArrayList<>();
//                    for (DataSnapshot data : snapshot.getChildren()) {
//                        LocationData location = data.getValue(LocationData.class);
//                        if (location != null) {
//                            LatLng latLng = new LatLng(location.latitude, location.longitude);
//                            mMap.addMarker(new MarkerOptions().position(latLng).title("Blind Cane"));
//                            pathPoints.add(latLng);
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                        }
//                    }
//                    // Vẽ đường đi nếu có ít nhất 2 điểm
//                    if (pathPoints.size() >= 2) {
//                        PolylineOptions polylineOptions = new PolylineOptions()
//                                .addAll(pathPoints) // Thêm tất cả các điểm
//                                .width(10) // Độ dày của đường
//                                .color(Color.BLUE); // Màu của đường
//                        mMap.addPolyline(polylineOptions);
//                    }
//                    // Di chuyển camera đến điểm đầu tiên hoặc trung tâm
//                    if (!pathPoints.isEmpty()) {
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.get(0), 15));
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });
            mMap.clear();
            waypoints.add(new LatLng(16.090387, 108.142925));
            waypoints.add(new LatLng(16.091500, 108.142000));
            waypoints.add(new LatLng(16.092765, 108.141018));
            waypoints.add(new LatLng(16.093610, 108.139846));

            for (LatLng point : waypoints) {
                mMap.addMarker(new MarkerOptions().position(point).title("Điểm dừng"));
            }
            if (!waypoints.isEmpty()) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 15));
            }
            getRoute();
        }
    }

    private void getRoute() {
        if (waypoints.size() < 2) return;
        Log.d("DEBUG", "HELO_get");
        StringBuilder urlBuilder = new StringBuilder("https://api.openrouteservice.org/v2/directions/driving-car?api_key=").append(API_KEY);
        urlBuilder.append("&start=").append(waypoints.get(0).longitude).append(",").append(waypoints.get(0).latitude);
        urlBuilder.append("&end=").append(waypoints.get(waypoints.size() - 1).longitude).append(",").append(waypoints.get(waypoints.size() - 1).latitude);
        Log.d("DEBUG", String.valueOf(urlBuilder));
        if (waypoints.size() > 2) {
            urlBuilder.append("&via=");
            for (int i = 1; i < waypoints.size() - 1; i++) {
                urlBuilder.append(waypoints.get(i).longitude).append(",").append(waypoints.get(i).latitude);
                if (i < waypoints.size() - 2) urlBuilder.append("|");
            }
        }

        String url = urlBuilder.toString();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                this::drawRoute,
                error -> Log.e("MapError", "Lỗi lấy dữ liệu đường đi: " + error.getMessage()));

        requestQueue.add(stringRequest);
    }

    private void drawRoute(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray routes = jsonResponse.getJSONArray("features");
            Log.d("DEBUG", String.valueOf(routes.length()));
            if (routes.length() > 0) {
                JSONArray coordinates = routes.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates");
                List<LatLng> polylinePoints = new ArrayList<>();
                Log.d("DEBUG", "Coordinates length: " + coordinates.length());

                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray point = coordinates.getJSONArray(i);
                    double lon = point.getDouble(0);
                    double lat = point.getDouble(1);
                    polylinePoints.add(new LatLng(lat, lon));
                }

                Log.d("DEBUG", "Polyline points: " + polylinePoints.size());
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(polylinePoints)
                        .width(10)
                        .color(Color.BLUE);

                if (mMap == null) {
                    Log.e("MapError", "GoogleMap is null");
                } else {
                    mMap.addPolyline(polylineOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylinePoints.get(0), 15));
                    Log.d("DEBUG", "Polyline added successfully");
                }
            } else {
                Log.e("MapError", "No routes found in response");
            }
        } catch (JSONException e) {
            Log.e("MapError", "Lỗi phân tích dữ liệu JSON: " + e.getMessage());
        }
    }

    private BitmapDescriptor createGreenDotIcon() {
        int size = 50; // Kích thước chấm tròn (pixel)
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

















































//private List<LatLng> decodePolyline(String encoded) {
//    List<LatLng> polyline = new ArrayList<>();
//    int index = 0, len = encoded.length();
//    int lat = 0, lng = 0;
//
//    while (index < len) {
//        int b, shift = 0, result = 0;
//        do {
//            b = encoded.charAt(index++) - 63;
//            result |= (b & 0x1f) << shift;
//            shift += 5;
//        } while (b >= 0x20);
//        int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
//        lat += dlat;
//
//        shift = 0;
//        result = 0;
//        do {
//            b = encoded.charAt(index++) - 63;
//            result |= (b & 0x1f) << shift;
//            shift += 5;
//        } while (b >= 0x20);
//        int dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
//        lng += dlng;
//
//        polyline.add(new LatLng(lat / 1E5, lng / 1E5));
//    }
//
//    return polyline;
//}