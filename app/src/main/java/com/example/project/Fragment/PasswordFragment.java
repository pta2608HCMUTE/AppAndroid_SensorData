package com.example.project.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.project.R;
import com.example.project.login;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordFragment extends Fragment {


    private TextInputEditText emailEditText;
    private MaterialButton resetPasswordButton;

    public PasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);

        emailEditText = view.findViewById(R.id.editTextEmail);
        resetPasswordButton = view.findViewById(R.id.buttonResetPassword);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        return view;
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Enter your email");
            emailEditText.requestFocus();
            return;
        }

        // Lấy thông tin người dùng hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Kiểm tra xem email nhập vào có trùng với email đang đăng nhập hay không
            if (email.equals(currentUser.getEmail())) {
                // Gửi email đặt lại mật khẩu
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Gửi email thành công
                                showSuccessDialog();
                            } else {
                                // Gửi email thất bại
                                showFailureDialog();
                            }
                        });
            } else {
                // Hiển thị thông báo lỗi về việc nhập sai email
                emailEditText.setError("Invalid email. Please enter the email associated with your account.");
                emailEditText.requestFocus();
            }
        } else {
            // Người dùng chưa đăng nhập, có thể xử lý tùy theo yêu cầu của bạn
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Success");
        builder.setMessage("Password reset email sent. Check your email.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Chuyển về LoginActivity
            Intent intent = new Intent(requireContext(), login.class);
            startActivity(intent);
            requireActivity().finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Error");
        builder.setMessage("Failed to send password reset email. Please try again.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Đóng dialog
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
