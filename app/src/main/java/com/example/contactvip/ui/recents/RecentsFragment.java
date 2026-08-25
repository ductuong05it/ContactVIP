package com.example.contactvip.ui.recents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.adapter.CallHistoryAdapter;
import com.example.contactvip.databinding.FragmentRecentsBinding;
import com.example.contactvip.viewmodel.CallHistoryViewModel;

public class RecentsFragment extends Fragment {
    private FragmentRecentsBinding binding;
    private CallHistoryViewModel viewModel;
    private CallHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CallHistoryViewModel.class);
        
        adapter = new CallHistoryAdapter();
        binding.recyclerViewRecents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRecents.setAdapter(adapter);
        
        viewModel.getAllCallHistory().observe(getViewLifecycleOwner(), calls -> {
            if (calls == null || calls.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewRecents.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewRecents.setVisibility(View.VISIBLE);
                adapter.submitList(calls);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
