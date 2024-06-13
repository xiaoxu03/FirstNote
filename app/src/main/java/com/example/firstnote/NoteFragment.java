package com.example.firstnote;

import static android.app.Activity.RESULT_OK;
import static com.example.firstnote.MainActivity.REQUEST_NOTE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    EditText search_input;
    AlertDialog.Builder builder;
    FloatingActionButton fab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = view.findViewById(R.id.new_note_button);
        noteListView = view.findViewById(R.id.note_list);
        empty_text = view.findViewById(R.id.empty_text);
        refresh_layout = view.findViewById(R.id.refresh_layout);
        refresh_header = view.findViewById(R.id.refresh_header);
        search_input = view.findViewById(R.id.search_input);

        MainActivity mainActivity = (MainActivity) getActivity();
        refresh_layout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                assert mainActivity != null;
                mainActivity.getNoteList();
                refresh_layout.finishRefresh();
            }
        });
        builder = new AlertDialog.Builder(getActivity());

        fab.setOnClickListener(v -> {
            final EditText title_input = new EditText(this.getContext());
            builder.setTitle("请输入笔记标题");
            builder.setView(title_input);
            builder.setPositiveButton("确定", (dialog, which) -> {
                //新建笔记
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("title", title_input.getText().toString());
                startActivity(intent);
                ((MainActivity) requireActivity()).need_flush = true;
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            });
            title_input.setText("");
            builder.show();
        });

        search_input.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String search = search_input.getText().toString();
                    List<Note> searchList = new ArrayList<>();
                    for (Note note : mList) {
                        if (note.getTitle().contains(search) || note.getFirst_line().contains(search) || note.getLabels().contains(search)) {
                            searchList.add(note);
                        }
                    }
                    noteAdapter.setNewData(searchList);
                }
            }
        );
    }

    public void flushNoteList() {
        noteAdapter.setNewData(mList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void initNoteList(List<Note> noteList) {
        Log.d("NoteFragment", "initNoteList: " + noteList.size());
        noteAdapter = new NoteAdapter(R.layout.note_item, mList, (MainActivity) getActivity());
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
        refresh_layout.finishRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
