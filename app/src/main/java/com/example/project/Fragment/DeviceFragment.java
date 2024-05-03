package com.example.project.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.project.MainActivity;
import com.example.project.MyFirebaseMessagingService;
import com.example.project.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

public class DeviceFragment extends Fragment {
    public static final String TAG = DeviceFragment.class.getSimpleName();
    private DatabaseReference databaseReference;
    private TextView nhietDoTextView, doAmTextView;
    private SeekBar nhietDoSeekBar, doAmSeekBar;
    private TextView nhietDoThresholdTextView, doAmThresholdTextView;
    private double nhietDoThreshold = 30;
    private double doAmThreshold = 70;
    private ImageView regBackImageView;
    private TextView tvRoomName, tvQuanly;
    private Button button;
    private DatabaseReference newRoomRef;

    private boolean setupSuccess = false;
    private BroadcastReceiver temperatureReceiver;

    private static final String CHANNEL_ID = "temperature_channel";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        // Ẩn thanh toolbar mặc định
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Hiển thị lại thanh toolbar khi Fragment được destroy
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        // Gán các thành phần từ layout
        nhietDoSeekBar = view.findViewById(R.id.nhietDoSeekBar);
        doAmSeekBar = view.findViewById(R.id.doAmSeekBar);
        nhietDoThresholdTextView = view.findViewById(R.id.nhietDoThresholdTextView);
        doAmThresholdTextView = view.findViewById(R.id.doAmThresholdTextView);
        nhietDoTextView = view.findViewById(R.id.nhietDoDht11);
        doAmTextView = view.findViewById(R.id.doAmDht11);
        regBackImageView = view.findViewById(R.id.reg_back);
        tvRoomName = view.findViewById(R.id.tvRoomName);
        tvQuanly = view.findViewById(R.id.tvQuanly);
        button = view.findViewById(R.id.button);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String nameRoom = bundle.getString("NameRoom");

            if (nameRoom != null) {
                // Hiển thị tên phòng trong TextView
                tvRoomName.setText(nameRoom);
                tvQuanly.setText("MANAGE YOUR " + nameRoom);
                newRoomRef = FirebaseDatabase.getInstance().getReference("sensorData").child(nameRoom);
                databaseReference = newRoomRef; // Gán databaseReference với reference của phòng hiện tại
            }
        }

        nhietDoSeekBar.setMax(100);
        doAmSeekBar.setMax(100);

        if (newRoomRef != null) {
            databaseReference = newRoomRef;
            // Lắng nghe sự thay đổi trong Firebase Realtime Database.
            newRoomRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Tạo path "data" nếu nó chưa tồn tại
                        DatabaseReference dataRef = newRoomRef.child("data");
                        dataRef.keepSynced(true); // Tùy chọn, giữ cho dữ liệu được đồng bộ hóa

                        // Lấy dữ liệu từ Firebase và cập nhật TextView.
                        Double nhietDo = dataSnapshot.child("data").child("nhietDo").getValue(Double.class);
                        Double doAm = dataSnapshot.child("data").child("doAm").getValue(Double.class);

                        if (nhietDo != null && doAm != null) {
                            String nhietDoText = String.format("%.1f °C", nhietDo);
                            String doAmText = String.format("%.1f %%", doAm);

                            nhietDoTextView.setText(nhietDoText);
                            doAmTextView.setText(doAmText);
                        } else {
                            // Xử lý khi giá trị nhiệt độ hoặc độ ẩm là null
                        }

                        // Lấy giá trị ngưỡng từ Firebase
                        Double nhietDoSetup = dataSnapshot.child("data").child("nhietDoSetup").getValue(Double.class);
                        Double doAmSetup = dataSnapshot.child("data").child("doAmSetup").getValue(Double.class);

                        if (nhietDoSetup != null && doAmSetup != null) {
                            nhietDoThresholdTextView.setText(String.format("Temperature Threshold: %.1f °C", nhietDoSetup));
                            doAmThresholdTextView.setText(String.format("Humidity Threshold: %.1f %%", doAmSetup));

                            // Cập nhật giá trị ngưỡng trên SeekBar.
                            nhietDoSeekBar.setProgress(nhietDoSetup.intValue());
                            doAmSeekBar.setProgress(doAmSetup.intValue());

                            // Hiển thị thông báo khi cần thiết
                            showThresholdAlerts(nhietDo, doAm, nhietDoSetup, doAmSetup);
                        } else {
                            // Xử lý khi giá trị ngưỡng là null
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Xử lý lỗi nếu có.
                }
            });


    } else {
            // Xử lý trường hợp databaseReference là null
            Log.e(TAG, "DatabaseReference is null");
        }


        // Thiết lập sự kiện lắng nghe cho ImageView back
        regBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để quay lại HomeFragment khi nhấn nút back
                goBackToHomeFragment();
            }
        });

        // Thiết lập sự kiện thay đổi giá trị cho SeekBar nhiệt độ.
        nhietDoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nhietDoThreshold = (double) progress;
                nhietDoThresholdTextView.setText(String.format("Temperature Threshold: %.1f °C", nhietDoThreshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Thiết lập sự kiện thay đổi giá trị cho SeekBar độ ẩm.
        doAmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                doAmThreshold = (double) progress;
                doAmThresholdTextView.setText(String.format("Humidity Threshold: %.1f %%", doAmThreshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Thiết lập sự kiện cho nút "SETUP"
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupThresholds(v);
            }
        });
    }

    private void sendAlertDataToFirebase(String alertPath) {
        if (databaseReference != null) {
            DatabaseReference alertRef = databaseReference.child("data").child(alertPath);
            alertRef.setValue(1);
        }
    }

    public void setupThresholds(View view) {
        // Lấy giá trị từ SeekBars
        int nhietDoThresholdValue = nhietDoSeekBar.getProgress();
        int doAmThresholdValue = doAmSeekBar.getProgress();

        // Gửi dữ liệu lên Firebase
        DatabaseReference thresholdsRef = databaseReference.child("data");
        thresholdsRef.child("nhietDoSetup").setValue(nhietDoThresholdValue);
        thresholdsRef.child("doAmSetup").setValue(doAmThresholdValue);

        // Hiển thị thông báo
        Snackbar.make(view, "Temperature and humidity thresholds updated successfully!", Snackbar.LENGTH_SHORT).show();
        setupSuccess = true; // Đánh dấu rằng quá trình setup đã thành công
    }

    private boolean nhietDoGreaterThanThreshold(Double nhietDo, Double nhietDoSetup) {
        return nhietDo != null && nhietDoSetup != null && nhietDo > nhietDoSetup;
    }


    private boolean doAmGreaterThanThreshold(Double doAm, Double doAmSetup) {
        return doAm != null && doAmSetup != null && doAm > doAmSetup;
    }

    private boolean doAmSmallThanThreshold(Double doAm, Double doAmSetup) {
        return doAm != null && doAmSetup != null && doAm < doAmSetup;
    }

    private boolean nhietDoSmallThanThreshold(Double nhietDo, Double nhietDoSetup) {
        return nhietDo != null && nhietDoSetup != null && nhietDo < nhietDoSetup;
    }

    private void showThresholdAlerts(Double nhietDo, Double doAm, Double nhietDoSetup, Double doAmSetup) {


        // Hiển thị thông báo khi chỉ nhiệt độ vượt ngưỡng
        if (nhietDoGreaterThanThreshold(nhietDo, nhietDoSetup)) {
            sendAlertDataToFirebase("tempmax");
        }
        // Hiển thị thông báo khi chỉ độ ẩm vượt ngưỡng
         if (doAmGreaterThanThreshold(doAm, doAmSetup)) {
            sendAlertDataToFirebase("hummax");
        }
         if (doAmSmallThanThreshold(doAm, doAmSetup)) {
            sendAlertData0ToFirebase("hummax");
        }
         if (nhietDoSmallThanThreshold(nhietDo, nhietDoSetup)) {
            sendAlertData0ToFirebase("tempmax");
        }
    }


    private void sendAlertData0ToFirebase(String alertPath) {
        if (databaseReference != null) {
            DatabaseReference alertRef = databaseReference.child("data").child(alertPath);
            alertRef.setValue(0);
        }
    }

    // Phương thức hiển thị AlertDialog.
    private void showAlertDialog(String title, String message) {
        if (isAdded() && getContext() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }


    private void goBackToHomeFragment() {
        // Đối với việc quay lại HomeFragment, bạn có thể sử dụng FragmentManager và popBackStack
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}
