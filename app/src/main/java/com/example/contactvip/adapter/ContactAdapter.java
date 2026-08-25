package com.example.contactvip.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.databinding.ItemContactBinding;
import com.example.contactvip.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ListAdapter<Contact, ContactAdapter.ContactViewHolder> {
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactAdapter(OnContactClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Contact> DIFF_CALLBACK = new DiffUtil.ItemCallback<Contact>() {
        @Override
        public boolean areItemsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
            return oldItem.phoneNumber.equals(newItem.phoneNumber) &&
                    oldItem.getFullName().equals(newItem.getFullName()) &&
                    (oldItem.avatarUri == null ? newItem.avatarUri == null : oldItem.avatarUri.equals(newItem.avatarUri));
        }
    };

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            Contact contact = getItem(i);
            String nameForIndexing = (contact.lastName != null && !contact.lastName.isEmpty()) 
                ? contact.lastName.toUpperCase() 
                : contact.firstName.toUpperCase();
            
            if (!nameForIndexing.isEmpty() && nameForIndexing.charAt(0) == section) {
                return i;
            }
        }
        return -1;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;

        public ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Contact contact, OnContactClickListener listener) {
            binding.tvContactName.setText(contact.getFullName());
            binding.tvPhoneNumber.setText(contact.phoneNumber);
            AvatarUtils.loadAvatar(itemView.getContext(), contact.avatarUri, contact.getFullName(), binding.ivAvatar);
            itemView.setOnClickListener(v -> listener.onContactClick(contact));
        }
    }
}
