package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, nameEditText;
    private MaterialButton createButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ImageView buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);


        // Ánh xạ các thành phần từ layout XML
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        nameEditText = findViewById(R.id.name);
        createButton = findViewById(R.id.loginbtn);
        buttonBack = findViewById(R.id.backButton);

        // Khởi tạo Firebase Authentication và Realtime Database
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Đặt sự kiện cho nút tạo tài khoản
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Quay lại LoginActivity khi ImageView được bấm
                navigateToLoginActivity();
            }
        });
    }

    private void registerUser() {
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        final String name = nameEditText.getText().toString();

        // Tạo tài khoản người dùng trên Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng kí thành công, lưu thông tin người dùng vào Realtime Database
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        saveUserToDatabase(user.getUid(), username, name);

                        // Hiển thị thông báo thành công
                        showMessage("Registration successful!");

                        navigateToLoginActivity();
                    } else {
                        // Đăng ký không thành công, xử lý lỗi
                        if (task.getException() instanceof FirebaseAuthException) {
                            // Xử lý lỗi đăng ký Firebase Authentication
                            String  errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            // Hiển thị thông báo không thành công
                            showMessage("Registration failed: " + errorCode);
                        }
                    }
                });
    }
    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, login.class);
        startActivity(intent);
        finish(); // Đóng màn hình đăng ký để không thể quay lại từ LoginActivity
    }

    private void saveUserToDatabase(String userId, String username, String name) {
        // Lưu thông tin người dùng vào Realtime Database
        User user = new User(username, name); // Sửa đổi constructor User để chấp nhận tên
        databaseReference.child(userId).setValue(user);
    }

    private void showMessage(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

