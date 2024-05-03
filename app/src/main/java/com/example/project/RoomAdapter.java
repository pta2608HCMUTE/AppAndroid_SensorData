package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private Context mContext;
    private SelectListener mOnClickItemListener;


    private List<Room> mListRoom;

    //Constructor
    public RoomAdapter(List<Room> mListRoom, SelectListener mOnClickItemListener) {
        this.mOnClickItemListener = mOnClickItemListener;
        this.mListRoom = mListRoom;
    }
    public RoomAdapter(List<Room> mListRoom) {
        this.mListRoom = mListRoom;
    }


    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = mListRoom.get(position);
        int itemPosition = holder.getAdapterPosition();
        if (room == null) {
            return;
        }

        holder.tv_room.setText(room.getName());



        holder.card_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mOnClickItemListener.onClickItemRoom(room, itemPosition);
            }
        });
        //Lang nghe longclick de xoa
        holder.card_room.setOnLongClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Room?")
                    .setMessage("Are you sure you want to delete this Room?")

                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {


                            if (currentPosition != RecyclerView.NO_POSITION) {

                                // Xóa dữ liệu phòng khỏi Firebase
                                deleteRoomFromFirebase(room);

                                // Xóa khỏi danh sách và cập nhật RecyclerView
                                mListRoom.remove(currentPosition);
                                notifyDataSetChanged();
                                holder.mainActivity.saveRoomList(Room.allRooms,"ListRoom");
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).setIcon(android.R.drawable.ic_menu_delete).show();
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return mListRoom.size();
    }


    // Phương thức để xóa dữ liệu phòng khỏi Firebase
    private void deleteRoomFromFirebase(Room room) {
        // Lấy tham chiếu đến Firebase Database của bạn (chắc chắn là đúng thư mục)
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("sensorData").child(room.getName());

        // Xóa dữ liệu phòng từ Firebase
        roomRef.removeValue();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {
        //display information about a room
        private LinearLayout layoutRoom;
        private TextView tv_room;
        private TextView tv_number_device;
        private CardView card_room;

        private MainActivity mainActivity;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoom = itemView.findViewById(R.id.layoutRoom);
            tv_room = itemView.findViewById(R.id.tv_room);
            card_room = itemView.findViewById(R.id.card_room);
            mainActivity = (MainActivity) itemView.getContext();
        }
    }
}
