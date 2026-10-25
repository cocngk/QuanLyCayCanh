package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.customer.ChatActivity;
import com.example.quanlycaycanh.data.Repository;

/** UC33 — Quản lý cuộc trò chuyện với khách hàng. */
public class AdminChatFragment extends Fragment {

    private Repository repo;
    private ChatThreadAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        RecyclerView rv = v.findViewById(R.id.rvDoanChat);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatThreadAdapter(c -> startActivity(new Intent(requireContext(), ChatActivity.class)
                .putExtra(ChatActivity.EXTRA_CUOC_TRO_CHUYEN, (long) c.maCuocTroChuyen)));
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && adapter != null) {
            adapter.submit(repo.conversations());
        }
    }
}
