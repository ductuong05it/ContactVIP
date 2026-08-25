package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.R;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.databinding.ActivityAddEditContactBinding;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.viewmodel.ContactViewModel;

public class AddEditContactActivity extends AppCompatActivity {
    private ActivityAddEditContactBinding binding;
    private ContactViewModel viewModel;
    private Contact existingContact;
    private String currentAvatarUri = null;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    currentAvatarUri = uri.toString();
                    try {
                        // Cấp quyền đọc lâu dài cho URI này
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        // Một số provider không hỗ trợ quyền vĩnh viễn, chúng ta bỏ qua nhưng vẫn lưu URI
                        e.printStackTrace();
                    }
                    AvatarUtils.loadAvatar(this, currentAvatarUri, getDisplayName(), binding.ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            binding.toolbar.setTitle(R.string.edit_contact);
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact != null && existingContact == null) {
                    existingContact = contact;
                    currentAvatarUri = contact.avatarUri;
                    populateFields(contact);
                }
            });
        }

        binding.btnChangePhoto.setOnClickListener(v -> 
            pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build())
        );
        binding.btnSave.setOnClickListener(v -> saveContact());
    }

    private void populateFields(Contact contact) {
        binding.etFirstName.setText(contact.firstName);
        binding.etLastName.setText(contact.lastName);
        binding.etPhone.setText(contact.phoneNumber);
        binding.etEmail.setText(contact.email);
        binding.etCompany.setText(contact.company);
        binding.etJobTitle.setText(contact.jobTitle);
        binding.etAddress.setText(contact.address);
        binding.etNotes.setText(contact.notes);
        AvatarUtils.loadAvatar(this, contact.avatarUri, contact.getFullName(), binding.ivAvatar);
    }

    private String getDisplayName() {
        String first = binding.etFirstName.getText().toString().trim();
        String last = binding.etLastName.getText().toString().trim();
        return (first + " " + last).trim();
    }

    private void saveContact() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        boolean hasError = false;
        if (firstName.isEmpty() && lastName.isEmpty()) {
            binding.tilFirstName.setError("First name or last name is required");
            hasError = true;
        } else {
            binding.tilFirstName.setError(null);
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Phone number is required");
            hasError = true;
        } else {
            binding.tilPhone.setError(null);
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            hasError = true;
        } else {
            binding.tilEmail.setError(null);
        }

        if (hasError) return;

        // Kiểm tra trùng số điện thoại trong luồng riêng
        new Thread(() -> {
            Contact duplicate = viewModel.getContactByPhoneNumber(phone);
            
            // Nếu tìm thấy số trùng và (đang tạo mới HOẶC số trùng không phải là ID hiện tại)
            if (duplicate != null && (existingContact == null || duplicate.id != existingContact.id)) {
                runOnUiThread(() -> {
                    binding.tilPhone.setError("This phone number already exists");
                    Toast.makeText(this, "Duplicate phone number!", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Nếu không trùng, tiến hành lưu
            runOnUiThread(() -> proceedToSave(firstName, lastName, phone, email));
        }).start();
    }

    private void proceedToSave(String firstName, String lastName, String phone, String email) {
        Contact contact = (existingContact != null) ? existingContact : new Contact();
        contact.firstName = firstName;
        contact.lastName = lastName;
        contact.phoneNumber = phone;
        contact.email = email;
        contact.company = binding.etCompany.getText().toString().trim();
        contact.jobTitle = binding.etJobTitle.getText().toString().trim();
        contact.address = binding.etAddress.getText().toString().trim();
        contact.notes = binding.etNotes.getText().toString().trim();
        contact.avatarUri = currentAvatarUri;
        contact.updatedAt = System.currentTimeMillis();

        if (existingContact == null) {
            contact.createdAt = System.currentTimeMillis();
            viewModel.insert(contact);
            Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(contact);
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
