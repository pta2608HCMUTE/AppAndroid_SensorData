package com.example.project;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView registerTextView;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        // Ánh xạ các thành phần từ layout XML
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginbtn);
        registerTextView = findViewById(R.id.register);

        // Khởi tạo Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Đặt sự kiện cho nút đăng nhập
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Đặt sự kiện cho TextView "Đăng kí"
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển đến màn hình đăng kí
                Intent intent = new Intent(login.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Kiểm tra xem có tên người dùng và mật khẩu không rỗng
        if (!username.isEmpty() && !password.isEmpty()) {
            // Sử dụng Firebase Authentication để đăng nhập
            firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công, chuyển đến MainActivity
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Đăng nhập không thành công, xử lý lỗi
                            Toast.makeText(login.this, "Đăng nhập không thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Hiển thị thông báo nếu tên người dùng hoặc mật khẩu rỗng
            Toast.makeText(login.this, "Vui lòng nhập tên người dùng và mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
}
