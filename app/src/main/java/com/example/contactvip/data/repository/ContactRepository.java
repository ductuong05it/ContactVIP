package com.example.contactvip.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.contactvip.data.dao.ContactDao;
import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.Contact;

import java.util.List;

public class ContactRepository {
    private final ContactDao contactDao;
    private final LiveData<List<Contact>> allContacts;

    public ContactRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        contactDao = db.contactDao();
        allContacts = contactDao.getAllContacts();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return allContacts;
    }

    public LiveData<List<Contact>> getFavoriteContacts() {
        return contactDao.getFavoriteContacts();
    }

    public LiveData<Contact> getContactById(long id) {
        return contactDao.getContactById(id);
    }

    public void insert(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> contactDao.insert(contact));
    }

    public void update(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> contactDao.update(contact));
    }

    public void delete(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> contactDao.delete(contact));
    }

    public LiveData<List<Contact>> searchContacts(String query) {
        return contactDao.searchContacts("%" + query + "%");
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        return contactDao.getContactByPhoneNumber(phoneNumber);
    }
}
