package com.example.gpsapp.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gpsapp.MainActivity;
import com.example.gpsapp.R;
import com.example.gpsapp.adapter.DeviceAdapter;
import com.example.gpsapp.model.Device;
import com.example.gpsapp.utils.CustomCaptureActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;


public class HomePage extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private TextView tvWelcome, tvName;
    private ImageView imAvt, imvAvt;
    private RecyclerView recyclerDevices;
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CardView btnAddDevice;
    private View loadingOverlay;
    private AlertDialog addDeviceDialog;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadingOverlay = findViewById(R.id.loading_overlay);
        drawerLayout = findViewById(R.id.drawer_layout);
        imAvt = findViewById(R.id.im_avt);
        navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        imvAvt = headerView.findViewById(R.id.imv_avt);
        tvName = headerView.findViewById(R.id.tvv_name);

        recyclerDevices = findViewById(R.id.recyclerDevices);
        recyclerDevices.setLayoutManager(new LinearLayoutManager(this));

        tvWelcome = findViewById(R.id.tv_welcome);
        imAvt = findViewById(R.id.im_avt);
        btnAddDevice = findViewById(R.id.btn_add_device);
        btnAddDevice.setOnClickListener(v -> showAddDeviceDialog());

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            loadUserData(user.getUid());
            loadDevices();
        }

        imAvt.setOnClickListener(v -> drawerLayout.open());

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "nav_home", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "nav_profile", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_change_pass) {
                Toast.makeText(this, "nav_chang_pass", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                firebaseAuth.signOut();
                startActivity(new Intent(HomePage.this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingOverlay.animate().alpha(1f).setDuration(200).start();
    }

    private void hideLoading() {
        loadingOverlay.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            loadingOverlay.setVisibility(View.GONE);
        }).start();
    }

    private void showAddDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_device, null);
        builder.setView(dialogView);

        addDeviceDialog = builder.create(); // Lưu dialog vào biến instance
        addDeviceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        addDeviceDialog.show();

        EditText etDeviceId = dialogView.findViewById(R.id.etDeviceId);
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnScanQR = dialogView.findViewById(R.id.btnScanQR);

        btnScanQR.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startQRScanner();
            }
        });

        btnOk.setOnClickListener(v -> {
            String deviceId = etDeviceId.getText().toString().trim();
            if (!deviceId.isEmpty()) {
                addDeviceToFirebase(deviceId);
                addDeviceDialog.dismiss();
                addDeviceDialog = null; // Đặt lại biến sau khi đóng
            } else {
                Toast.makeText(HomePage.this, "Vui lòng nhập hoặc quét ID thiết bị", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            addDeviceDialog.dismiss();
            addDeviceDialog = null; // Đặt lại biến sau khi đóng
        });
    }

    private void addDeviceToFirebase(String deviceId) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference deviceRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("devices")
                    .child(deviceId);

            deviceRef.setValue(deviceId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(HomePage.this, "Thêm thiết bị thành công", Toast.LENGTH_SHORT).show();
                    loadDevices();
                } else {
                    Toast.makeText(HomePage.this, "Lỗi khi thêm thiết bị", Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Lỗi khi thêm thiết bị", task.getException());
                }
            });
        }
    }

    private void loadUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileUrl = snapshot.child("profile").getValue(String.class);
                    tvWelcome.setText(name);
                    tvName.setText(name);
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(HomePage.this).load(profileUrl).into(imAvt);
                        Glide.with(HomePage.this).load(profileUrl).into(imvAvt);
                    } else {
                        imAvt.setImageResource(R.drawable.logo);
                    }
                }
            }
        });
    }

    private void loadDevices() {
        showLoading();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference devicesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("devices");

        devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deviceList = new ArrayList<>();
                int totalDevices = (int) snapshot.getChildrenCount();
                if (totalDevices == 0) {
                    updateRecyclerView();
                    hideLoading();
                    return;
                }

                final int[] loadedCount = {0};

                for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                    String deviceId = deviceSnapshot.getKey();
                    FirebaseDatabase.getInstance()
                            .getReference("devices")
                            .child(deviceId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot deviceSnapshot) {
                                    if (deviceSnapshot.exists()) {
                                        Device device = deviceSnapshot.getValue(Device.class);
                                        if (device != null) {
                                            device.setDeviceId(deviceId);
                                            Log.d("Deviceid", device.getDeviceId());
                                            deviceList.add(device);
                                        }
                                    }
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalDevices) {
                                        hideLoading();
                                        updateRecyclerView();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalDevices) {
                                        updateRecyclerView();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(HomePage.this, "Lỗi khi tải thiết bị: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView() {
        deviceAdapter = new DeviceAdapter(HomePage.this, deviceList);
        recyclerDevices.setAdapter(deviceAdapter);
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Cần cấp quyền camera để quét mã QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Quét mã QR trong khung vuông");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setCameraId(0);
        options.setBarcodeImageEnabled(false);
        options.setCaptureActivity(CustomCaptureActivity.class);
        barcodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String scannedDeviceId = result.getContents();
            if (addDeviceDialog != null && addDeviceDialog.isShowing()) {
                EditText etDeviceId = addDeviceDialog.findViewById(R.id.etDeviceId);
                if (etDeviceId != null) {
                    etDeviceId.setText(scannedDeviceId);
                }
            } else {
                // Nếu dialog không hiển thị, mở lại dialog và điền ID
                showAddDeviceDialog();
                new Handler().postDelayed(() -> {
                    if (addDeviceDialog != null && addDeviceDialog.isShowing()) {
                        EditText etDeviceId = addDeviceDialog.findViewById(R.id.etDeviceId);
                        if (etDeviceId != null) {
                            etDeviceId.setText(scannedDeviceId);
                        }
                    }
                }, 200); // Tăng thời gian delay để đảm bảo dialog được hiển thị
            }
            Toast.makeText(this, "Đã quét: " + scannedDeviceId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Hủy quét mã QR", Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addDeviceDialog != null && addDeviceDialog.isShowing()) {
            addDeviceDialog.dismiss();
            addDeviceDialog = null;
        }
    }

}
