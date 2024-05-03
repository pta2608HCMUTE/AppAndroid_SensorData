package com.example.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private String name;



    public Room(String name) {
        this.name = name;

    }


    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }




    public static List<Room> allRooms = new ArrayList<>();


}
