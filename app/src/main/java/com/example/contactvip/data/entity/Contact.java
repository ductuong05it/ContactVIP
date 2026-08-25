package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public String email;
    public String company;
    public String jobTitle;
    public String address;
    public String notes;
    public String avatarUri;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public String getFullName() {
        String first = (firstName != null) ? firstName : "";
        String last = (lastName != null) ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? phoneNumber : full;
    }
}
