package com.example.project.Fragment;



import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.example.project.MainActivity;
import com.example.project.R;
import com.example.project.Room;
import com.example.project.RoomAdapter;
import com.example.project.SelectListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private RecyclerView rcvRoom;
    private View view;
    private FloatingActionButton btnAddRoom;

    private MainActivity mainActivity;
    private RoomAdapter roomAdapter;

    @Override
    public void onResume() {
        roomAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = (MainActivity) getActivity();
        rcvRoom = view.findViewById(R.id.rcvRoom);

        //The Room.allRooms list is loaded from shared preferences using the mainActivity.getRoomList("listRoom")
        Room.allRooms =  mainActivity.getRoomList("ListRoom");

        //set on the rcvRoom to manage the layout of the RecLyclerView.
        LinearLayoutManager LinearLayout = new LinearLayoutManager(mainActivity);
        rcvRoom.setLayoutManager(LinearLayout);

        roomAdapter = new RoomAdapter(Room.allRooms, new SelectListener() {
            @Override
            public void onClickItemRoom(Room room, int position) {
                mainActivity.gotoDeviceFragment(room.getName().toString(), position);
            }
        });
        rcvRoom.setAdapter(roomAdapter);

        btnAddRoom = view.findViewById(R.id.btnAdd);
        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTabToAddRoom();
            }


        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);

            private void showTabToAddRoom() {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View diaLogView = getLayoutInflater().inflate(R.layout.tab_add_room, null);
                builder.setView(diaLogView);
                builder.setTitle("Add Room");


                final EditText edt_add_Room = diaLogView.findViewById(R.id.edt_add_Room);
                builder.setPositiveButton("Add Room", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String roomName = edt_add_Room.getText().toString();
                        boolean exited = false;
                        for (Room room : Room.allRooms) { //If the room name is already in use, a toast message is shown to notify the user.
                            if (room.getName().equals(roomName)) {
                                Toast.makeText(getActivity(), "This room is exited", Toast.LENGTH_SHORT).show();
                                exited = true;
                                break;
                            }
                        }
                        if (!exited) { //a new room is created and added to the list of rooms
                            Room.allRooms.add(new Room(edt_add_Room.getText().toString()));
                            mainActivity.saveRoomList(Room.allRooms, "ListRoom"); //the list is saved in shared preferences.
                            roomAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    return view;
    }}