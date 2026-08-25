package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.adapter.ContactAdapter;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.databinding.FragmentContactsBinding;
import com.example.contactvip.viewmodel.ContactViewModel;

public class ContactsFragment extends Fragment implements ContactAdapter.OnContactClickListener {
    private FragmentContactsBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        
        adapter = new ContactAdapter(this);
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewContacts.setAdapter(adapter);
        
        viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts == null || contacts.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewContacts.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewContacts.setVisibility(View.VISIBLE);
                adapter.submitList(contacts);
            }
        });

        binding.btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditContactActivity.class);
            startActivity(intent);
        });

        binding.alphabetIndex.setOnIndexSelectedListener(letter -> {
            int position = adapter.getPositionForSection(letter);
            if (position != -1) {
                ((LinearLayoutManager) binding.recyclerViewContacts.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContacts(newText);
                return true;
            }
        });
    }

    private void searchContacts(String query) {
        if (query.isEmpty()) {
            viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> adapter.submitList(contacts));
        } else {
            viewModel.searchContacts(query).observe(getViewLifecycleOwner(), contacts -> adapter.submitList(contacts));
        }
    }

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
        intent.putExtra("CONTACT_ID", contact.id);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
