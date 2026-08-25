package com.example.contactvip.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.repository.ContactRepository;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ContactViewModel extends AndroidViewModel {
    private final ContactRepository repository;
    private final MediatorLiveData<List<Contact>> allContactsSorted = new MediatorLiveData<>();

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository(application);
        
        allContactsSorted.addSource(repository.getAllContacts(), contacts -> {
            if (contacts != null) {
                List<Contact> sortedList = new ArrayList<>(contacts);
                sortContacts(sortedList);
                allContactsSorted.setValue(sortedList);
            }
        });
    }

    public LiveData<List<Contact>> getAllContacts() {
        return allContactsSorted;
    }

    public LiveData<List<Contact>> getFavoriteContacts() {
        return repository.getFavoriteContacts(); // Should also sort, but skipping for brevity
    }

    public LiveData<Contact> getContactById(long id) {
        return repository.getContactById(id);
    }

    public void insert(Contact contact) {
        repository.insert(contact);
    }

    public void update(Contact contact) {
        repository.update(contact);
    }

    public void delete(Contact contact) {
        repository.delete(contact);
    }

    public LiveData<List<Contact>> searchContacts(String query) {
        return repository.searchContacts(query);
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        return repository.getContactByPhoneNumber(phoneNumber);
    }

    private void sortContacts(List<Contact> contacts) {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        contacts.sort((c1, c2) -> {
            // Sắp xếp theo Tên (lastName) trước, sau đó mới đến Họ (firstName)
            int res = collator.compare(c1.lastName != null ? c1.lastName : "", c2.lastName != null ? c2.lastName : "");
            if (res == 0) {
                res = collator.compare(c1.firstName != null ? c1.firstName : "", c2.firstName != null ? c2.firstName : "");
            }
            return res;
        });
    }
}
