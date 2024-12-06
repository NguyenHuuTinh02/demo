package com.examples.yumbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivitySignUpBinding;
import com.examples.yumbox.Model.UserModel;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private Dialog loadingDialog;
    private GoogleSignInClient googleSignInClient;

    // User info
    private String email;
    private String password;
    private String username;
    private String address;
    private String phone;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = LoadingDialog.create(this, "Đang tạo tài khoản...");

        // Init Google Sign-In client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Button create account
        binding.createAccountButton.setOnClickListener(v -> {
            // Get user input
            username = binding.username.getText().toString().trim();
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            // Check
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                createAccount(email, password);
            }
        });

        // Button Google Sign-In
        binding.googleButton.setOnClickListener(v -> {
            Intent signIntent = googleSignInClient.getSignInIntent();
            launcher.launch(signIntent);
        });

        // Move to LoginActivity
        binding.haveAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
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
                      showToast("Đăng ký Google thành công");
                      user = auth.getCurrentUser();
                      String email = user != null ? user.getEmail() : null;
                      saveUserData(email);
                      startActivity(new Intent(this, LoginActivity.class));
                      finish();
                  } else {
                      showToast("Đăng ký Google thất bại");
                  }
               });
           }
       }
    });

    private void createAccount(String email, String password) {
        loadingDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Tạo tài khoản thành công");
                saveUserData(null);
                loadingDialog.dismiss();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                showToast("Tạo tài khoản thất bại");
                loadingDialog.dismiss();
            }
        });
    }

    private void saveUserData(String email) {
        if (email == null) {
            // Get user input
            username = binding.username.getText().toString().trim();
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();
        }

        if (auth.getCurrentUser() != null) {
            UserModel user = new UserModel(username, email, password, address, phone);
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseRef.child("Users").child(userID).setValue(user);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}