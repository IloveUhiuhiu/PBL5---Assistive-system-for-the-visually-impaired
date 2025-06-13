package com.example.gpsapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.gpsapp.model.LocationData;
import com.example.gpsapp.socket.FirebaseService;
import com.example.gpsapp.socket.WebSocketService;
import com.example.gpsapp.view.HomePage;
import com.example.gpsapp.view.SignupPage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 20;
    private static final String DEVICE_ID = "blind_cane_001";
    private static final String FAKE_NAME = "Chu tu";
    private static final double FAKE_LATITUDE = 16.0843559;
    private static final double FAKE_LONGITUDE = 108.1438971;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient googleSignInClient;
    private CardView googleSignInCard, emailSignInCard;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUI();
        initializeFirebase();
        initializeGoogleSignIn();
        setupClickListeners();
        startWebSocketService();
    }

    private void initializeUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        googleSignInCard = findViewById(R.id.cv_signup_google);
        emailSignInCard = findViewById(R.id.cv_signup_email);
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        googleSignInCard.setOnClickListener(v -> signInWithGoogle());
        emailSignInCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupPage.class);
            startActivity(intent);
        });
    }

    private void startWebSocketService() {
        Intent serviceIntent = new Intent(this, FirebaseService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            navigateToHomePage();
        }
    }

    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                showConfirmationDialog(account);
            }
        } catch (ApiException e) {
            showToast("Đăng nhập thất bại: " + e.getMessage());
        }
    }

    private void showConfirmationDialog(GoogleSignInAccount account) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng nhập")
                .setMessage("Bạn có muốn tiếp tục đăng nhập với tài khoản: " + account.getEmail() + "?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> authenticateWithFirebase(account.getIdToken()))
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                    googleSignInClient.signOut();
                })
                .setCancelable(false)
                .show();
    }

    private void authenticateWithFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleSuccessfulSignIn(firebaseAuth.getCurrentUser());
                    } else {
                        showToast("Đăng nhập thất bại!");
                    }
                });
    }

    private void handleSuccessfulSignIn(FirebaseUser user) {
        if (user != null) {
            userId = user.getUid();
            saveUserData(user);
            // sendFakeLocation();
            navigateToHomePage();
        }
    }

    private void saveUserData(FirebaseUser user) {
        DatabaseReference userRef = database.getReference().child("users").child(userId);
        userRef.child("id").setValue(user.getUid());
        userRef.child("name").setValue(user.getDisplayName());
        userRef.child("profile").setValue(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendFakeLocation() {
        DatabaseReference locationRef = database.getReference()
                .child("users")
                .child(userId)
                .child("devices")
                .child(DEVICE_ID);

        LocationData location = new LocationData(
                FAKE_NAME,
                FAKE_LATITUDE,
                FAKE_LONGITUDE,
                System.currentTimeMillis()
        );

        locationRef.push().setValue(location);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}