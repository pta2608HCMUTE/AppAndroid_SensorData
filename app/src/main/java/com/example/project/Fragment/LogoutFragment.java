package com.example.project.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.project.R;
import com.example.project.login;

public class LogoutFragment extends Fragment {


    public LogoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị dialog xác nhận trước khi đăng xuất
        showLogoutConfirmationDialog();

    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Log Out", (dialog, which) -> {
            // Người dùng đã xác nhận đăng xuất, thực hiện đăng xuất
            navigateToLoginActivity();
        });
        builder.setNegativeButton("Cancle", (dialog, which) -> {
            // Người dùng đã hủy bỏ đăng xuất, đóng dialog
            dialog.dismiss();
        });
        builder.show();
    }





    private void navigateToLoginActivity() {
        // Tạo Intent để mở LoginActivity
        Intent intent = new Intent(getActivity(), login.class);
        // Xóa tất cả các Activity khác và đặt LoginActivity làm Activity mới
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Bắt đầu LoginActivity
        startActivity(intent);
        // Kết thúc Fragment hiện tại
        getActivity().finish();
    }
}
