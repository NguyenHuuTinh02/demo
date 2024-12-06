package com.examples.yumbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityLoginBinding;
import com.examples.yumbox.Utils.LoadingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Dialog loadingDialog;
    private GoogleSignInClient googleSignInClient;

    // User info
    private String username;
    private String email;
    private String address;
    private String phone;
    private String password;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private FirebaseUser user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = LoadingDialog.create(this, "Đang đăng nhập...");

        // Init Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Button login
        binding.loginButton.setOnClickListener(v -> {
            // Get user input
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            // Check
            if (email.isBlank() || password.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                loginUserAccount(email, password);
            }
        });

        // Button Google Sign-In
        binding.googleButton.setOnClickListener(v -> {
            Intent signIntent = googleSignInClient.getSignInIntent();
            launcher.launch(signIntent);
        });

        // Move to SignUpActivity
        binding.dontHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // Launcher Google Sign-In
    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(credential).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        user = auth.getCurrentUser();
                        userID = user != null ? user.getUid() : null;
                        checkUserRole(userID);
                    } else {
                        showToast("Đăng nhập thất bại");
                    }
                });
            }
        }
    });

    private void loginUserAccount(String email, String password) {
        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                userID = user != null ? user.getUid() : null;
                checkUserRole(userID);
                loadingDialog.dismiss();
            } else {
                showToast("Email hoặc mật khẩu không đúng");
                loadingDialog.dismiss();
            }
        });
    }

    private void checkUserRole(String userID) {
        databaseRef.child("Users").child(userID).child("role").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userRole = "";
                    if (snapshot.getValue() != null) {
                        userRole = snapshot.getValue().toString();
                    }
                    if (userRole.equals("customer")) {
                        showToast("Đăng nhập thành công");
                        updateUI(user);
                    } else {
                        auth.signOut();
                        showToast("Đăng nhập thất bại");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
        if (user != null) {
            updateUI(user);
        }
    }

    private void updateUI(FirebaseUser user) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}