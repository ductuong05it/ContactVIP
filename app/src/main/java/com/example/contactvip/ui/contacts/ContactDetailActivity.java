package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactPhone;
import com.example.contactvip.databinding.ActivityContactDetailBinding;
import com.example.contactvip.databinding.ItemPhoneDetailBinding;
import com.example.contactvip.ui.call.CallActivity;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.viewmodel.ContactViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ContactDetailActivity extends AppCompatActivity {
    private ActivityContactDetailBinding binding;
    private ContactViewModel viewModel;
    private Contact currentContact;
    private List<ContactPhone> currentPhones;

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
            
            // Observe groups
            viewModel.getGroupsForContact(contactId).observe(this, groups -> {
                updateGroupChips(groups);
            });
            
            // Fetch phones
            new Thread(() -> {
                currentPhones = viewModel.getPhonesForContactSync(contactId);
                runOnUiThread(this::displayPhones);
            }).start();

        } else {
            finish();
        }

        binding.btnCall.setOnClickListener(v -> startCallDefault());
        binding.btnMessage.setOnClickListener(v -> startSms());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnShare.setOnClickListener(v -> shareContact());
        binding.btnEdit.setOnClickListener(v -> editContact());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void displayContact(Contact contact) {
        binding.tvName.setText(contact.name);
        AvatarUtils.loadAvatar(this, contact.avatarUri, binding.ivAvatar);
        
        setupSection(binding.tvEmail, contact.email, "Email: ");
        setupSection(binding.tvCompany, contact.company, "Company: ");
        setupSection(binding.tvNotes, contact.notes, "Notes: ");
        
        binding.btnFavorite.setAlpha(contact.isFavorite ? 1.0f : 0.5f);
    }

    private void displayPhones() {
        binding.phonesContainer.removeAllViews();
        if (currentPhones != null) {
            for (ContactPhone phone : currentPhones) {
                ItemPhoneDetailBinding pb = ItemPhoneDetailBinding.inflate(LayoutInflater.from(this), binding.phonesContainer, false);
                pb.tvLabel.setText(phone.label);
                pb.tvPhone.setText(phone.phoneNumber);
                pb.btnCall.setOnClickListener(v -> startCall(phone.phoneNumber));
                binding.phonesContainer.addView(pb.getRoot());
            }
        }
    }

    private void updateGroupChips(List<ContactGroup> groups) {
        binding.groupChips.removeAllViews();
        if (groups != null && !groups.isEmpty()) {
            binding.groupsContainer.setVisibility(View.VISIBLE);
            for (ContactGroup group : groups) {
                Chip chip = new Chip(this);
                chip.setText(group.name);
                binding.groupChips.addView(chip);
            }
        } else {
            binding.groupsContainer.setVisibility(View.GONE);
        }
    }

    private void setupSection(android.widget.TextView textView, String value, String prefix) {
        if (value != null && !value.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(prefix + value);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void startCallDefault() {
        if (currentPhones != null && !currentPhones.isEmpty()) {
            ContactPhone primary = currentPhones.get(0);
            for (ContactPhone p : currentPhones) if (p.isPrimary) primary = p;
            startCall(primary.phoneNumber);
        } else {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCall(String number) {
        if (currentContact == null) return;
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("PHONE_NUMBER", number);
        intent.putExtra("CONTACT_NAME", currentContact.getFullName());
        intent.putExtra("CONTACT_ID", currentContact.id);
        startActivity(intent);
    }

    private void shareContact() {
        if (currentContact == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(currentContact.getFullName()).append("\n");
        if (currentPhones != null) {
            for (ContactPhone p : currentPhones) {
                sb.append(p.label).append(": ").append(p.phoneNumber).append("\n");
            }
        }
        if (currentContact.email != null && !currentContact.email.isEmpty()) sb.append("Email: ").append(currentContact.email).append("\n");
        if (currentContact.company != null && !currentContact.company.isEmpty()) sb.append("Company: ").append(currentContact.company).append("\n");

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(sendIntent, "Share Contact"));
    }

    private void startSms() {
        if (currentContact == null || currentPhones == null || currentPhones.isEmpty()) return;
        try {
            ContactPhone primary = currentPhones.get(0);
            for (ContactPhone p : currentPhones) if (p.isPrimary) primary = p;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + primary.phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        if (currentContact == null) return;
        currentContact.isFavorite = !currentContact.isFavorite;
        currentContact.updatedAt = System.currentTimeMillis(); // Cập nhật mốc thời gian để danh sách bên ngoài refresh
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
