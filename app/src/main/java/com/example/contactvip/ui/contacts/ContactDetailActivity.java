package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.databinding.ActivityContactDetailBinding;
import com.example.contactvip.ui.call.CallActivity;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.viewmodel.ContactViewModel;

public class ContactDetailActivity extends AppCompatActivity {
    private ActivityContactDetailBinding binding;
    private ContactViewModel viewModel;
    private Contact currentContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact != null) {
                    currentContact = contact;
                    displayContact(contact);
                }
            });
        } else {
            finish();
        }

        binding.btnCall.setOnClickListener(v -> startCall());
        binding.btnMessage.setOnClickListener(v -> startSms());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnEdit.setOnClickListener(v -> editContact());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void displayContact(Contact contact) {
        binding.tvName.setText(contact.getFullName());
        AvatarUtils.loadAvatar(this, contact.avatarUri, contact.getFullName(), binding.ivAvatar);
        
        binding.tvPhone.setText(contact.phoneNumber);
        
        setupSection(binding.tvEmail, contact.email, "Email: ");
        setupSection(binding.tvCompany, contact.company, "Company: ");
        setupSection(binding.tvNotes, contact.notes, "Notes: ");
        
        binding.btnFavorite.setAlpha(contact.isFavorite ? 1.0f : 0.5f);
    }

    private void setupSection(android.widget.TextView textView, String value, String prefix) {
        if (value != null && !value.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(prefix + value);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void startCall() {
        if (currentContact == null) return;
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("PHONE_NUMBER", currentContact.phoneNumber);
        intent.putExtra("CONTACT_NAME", currentContact.getFullName());
        intent.putExtra("CONTACT_ID", currentContact.id);
        startActivity(intent);
    }

    private void startSms() {
        if (currentContact == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + currentContact.phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        if (currentContact == null) return;
        currentContact.isFavorite = !currentContact.isFavorite;
        viewModel.update(currentContact);
    }

    private void editContact() {
        if (currentContact == null) return;
        Intent intent = new Intent(this, AddEditContactActivity.class);
        intent.putExtra("CONTACT_ID", currentContact.id);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (currentContact == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + currentContact.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(currentContact);
                    Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
