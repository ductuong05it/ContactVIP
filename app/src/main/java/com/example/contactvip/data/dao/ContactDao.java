package com.example.contactvip.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.contactvip.data.entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert
    long insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts ORDER BY lastName ASC, firstName ASC")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY lastName ASC, firstName ASC")
    LiveData<List<Contact>> getFavoriteContacts();

    @Query("SELECT * FROM contacts WHERE firstName LIKE :query OR lastName LIKE :query OR phoneNumber LIKE :query")
    LiveData<List<Contact>> searchContacts(String query);

    @Query("SELECT * FROM contacts WHERE id = :id")
    LiveData<Contact> getContactById(long id);

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    Contact getContactByPhoneNumber(String phoneNumber);
}
