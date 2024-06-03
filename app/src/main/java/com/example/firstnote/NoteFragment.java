package com.example.firstnote;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoteFragment extends Fragment {
    NoteAdapter noteAdapter;
    RecyclerView noteListView;
    List<Note> mList = new ArrayList<>();
    LinearLayout empty_text;
    RefreshLayout refresh_layout;
    RefreshHeader refresh_header;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.new_note_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到新建笔记页面
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                startActivity(intent);
            }
        });
        noteListView = view.findViewById(R.id.note_list);
        empty_text = view.findViewById(R.id.empty_text);
        refresh_layout = view.findViewById(R.id.refresh_layout);
        refresh_header = view.findViewById(R.id.refresh_header);

        MainActivity mainActivity = (MainActivity) getActivity();
        refresh_layout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                assert mainActivity != null;
                mainActivity.getNoteList();
                refresh_layout.finishRefresh();
            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public void initNoteList(List<Note> noteList) {
        Log.d("NoteFragment", "initNoteList: " + noteList.size());
        noteAdapter = new NoteAdapter(R.layout.note_item, mList);
        noteListView.setLayoutManager(new LinearLayoutManager(getContext()));
        noteListView.setAdapter(noteAdapter);

        if (!noteList.isEmpty()) {
            mList.clear();
            mList.addAll(noteList);
            noteAdapter.notifyDataSetChanged();
            noteListView.setVisibility(View.VISIBLE);
            empty_text.setVisibility(View.GONE);
        } else {
            noteListView.setVisibility(View.GONE);
            empty_text.setVisibility(View.VISIBLE);
        }
        noteAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            //长按事件
            ImageButton delete_button = view.findViewById(R.id.delete_button);
            Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            delete_button.startAnimation(fadeIn);
            delete_button.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // 在Runnable中，设置按钮的可见性为View.GONE
                    Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                    delete_button.startAnimation(fadeOut);
                    delete_button.setVisibility(View.GONE);
                }
            };

            // 使用Handler的postDelayed方法来在3秒后执行Runnable
            handler.postDelayed(runnable, 3000);
            return true;
        });
        refresh_layout.finishRefresh();
    }
}
