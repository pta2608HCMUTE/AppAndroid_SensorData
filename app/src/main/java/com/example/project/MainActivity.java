package com.example.project;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.example.project.Fragment.PasswordFragment;
import com.example.project.Fragment.DeviceFragment;
import com.example.project.Fragment.HomeFragment;
import com.example.project.Fragment.LogoutFragment;
import com.example.project.Fragment.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_HOME = 0;

    private static final int FRAGMENT_LOGOUT = 1;
    private static final int FRAGMENT_MY_PROFILE = 2;
    private static final int FRAGMENT_CHANGE_PASSWORD = 3;
    // create for current fragment
    private int currentFragment = FRAGMENT_HOME;

    private DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    private static final String CHANNEL_ID = "temperature_channel";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent.hasExtra("NameRoom") && intent.hasExtra("Index")) {
            String roomName = intent.getStringExtra("NameRoom");
            int index = intent.getIntExtra("Index", -1);

            if (index != -1) {
                // Chuyển đến DeviceFragment với thông tin phòng và chỉ số
                gotoDeviceFragment(roomName, index);
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);


        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        replaceFragment(new HomeFragment());

        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.usernameTextView);
        TextView nameTextView = headerView.findViewById(R.id.nameTextView);

        // Đăng ký BroadcastReceiver
        MyFirebaseMessagingService temperatureReceiver = new MyFirebaseMessagingService();
        IntentFilter intentFilter = new IntentFilter("com.example.project.TEMPERATURE_THRESHOLD");
        registerReceiver(temperatureReceiver, intentFilter);

        // Tạo Notification Channel (Chỉ cần thực hiện một lần)
        createNotificationChannel();

        // Lấy thông tin người dùng từ Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();


        if (currentUser != null) {
            // Người dùng đã đăng nhập
            String username = currentUser.getEmail();
            String userId = currentUser.getUid();

            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Hiển thị thông tin trong TextViews
                            usernameTextView.setText(user.getUsername());
                            nameTextView.setText(user.getName());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần thiết
                }
            });


            // Check for temperature and humidity alerts
            DatabaseReference sensorDataRef = FirebaseDatabase.getInstance().getReference("sensorData").child("LIVING ROOM").child("data");
            sensorDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Integer tempmax = dataSnapshot.child("tempmax").getValue(Integer.class);
                        Integer hummax = dataSnapshot.child("hummax").getValue(Integer.class);


                        Log.d("FirebaseData", "tempmax: " + tempmax + ", hummax: " + hummax);

                        if(tempmax == 1 && hummax == 1) {
                            showNotification("Temperature and Humidity Alert", "Temperature and humidity both exceed the set thresholds!");
                        }
                        else if (tempmax != null && tempmax == 1) {
                            showNotification("Temperature Alert", "Temperature exceeds the set threshold!");
                        }

                       else if (hummax != null && hummax == 1) {
                            showNotification("Humidity Alert", "Humidity exceeds the set threshold!");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Xử lý lỗi nếu cần thiết
                }
            });
        } else {
            // Người dùng chưa đăng nhập, xử lý tùy ý
        }
    }

        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            openHomeFragment();
        } else if (id == R.id.nav_logout) {
            openLogoutFragment();
        } else if (id == R.id.nav_my_profile) {
            openMyProfileFragment();
        } else if (id == R.id.nav_change_password) {
            openChangePasswordFragment();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void openHomeFragment() {
        if (currentFragment != FRAGMENT_HOME) {
            replaceFragment(new HomeFragment());
            currentFragment = FRAGMENT_HOME;
        }
    }
    private void openLogoutFragment() {
        if (currentFragment != FRAGMENT_LOGOUT) {
            replaceFragment(new LogoutFragment());
            currentFragment = FRAGMENT_LOGOUT;
        }
    }

    private void openMyProfileFragment() {
        if (currentFragment != FRAGMENT_MY_PROFILE) {
            replaceFragment(new ProfileFragment());
            currentFragment = FRAGMENT_MY_PROFILE;

        }
    }

    private void openChangePasswordFragment() {
        if (currentFragment != FRAGMENT_CHANGE_PASSWORD) {
            replaceFragment(new PasswordFragment());
            currentFragment = FRAGMENT_CHANGE_PASSWORD;
        }
    }

    private void showNotification(String title, String message) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        // Tạo intent để mở ứng dụng khi người dùng chạm vào thông báo
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        intent.putExtra("NameRoom", "LIVING ROOM");
        // Truyền chỉ số của phòng LIVING ROOM (ở đây là 1) vào intent
        intent.putExtra("Index", 1);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_icon_facebook)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // The '0' here is the notification ID, you might want to use a unique ID.
            notificationManager.notify(0, notification);
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Temperature Channel";
            String description = "Channel for temperature alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    public void gotoDeviceFragment(String nameRoom , int index){

        FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
        DeviceFragment deviceFragment  = new DeviceFragment();

        Bundle bundle = new Bundle();
        bundle.putString("NameRoom" , nameRoom);
        bundle.putInt("index" , index);

        deviceFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.content_frame, deviceFragment);
        fragmentTransaction.addToBackStack(DeviceFragment.TAG);
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame , fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void saveRoomList(List<Room> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
        Toast.makeText(MainActivity.this, "Save complete", Toast.LENGTH_SHORT).show();
    }


    public List<Room> getRoomList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        if (json != null) {
            Type type = new TypeToken<List<Room>>() {}.getType();
            List<Room> roomList = gson.fromJson(json, type);
            return roomList;
        } else {
            return new ArrayList<>();
        }
    }
}