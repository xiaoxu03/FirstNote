package com.example.firstnote;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.layout.api.RefreshHeader;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ViewPager2 mainViewPager2;
    TabLayout tl;
    //获取笔记列表
    List<Note> noteList = new ArrayList<Note>();
    RefreshHeader refreshHeader;
    static final int GET_NOTE_LIST = 100;

    private final List<Fragment> fragments = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton setting_button = findViewById(R.id.settings_button);
        setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        fragments.add(new NoteFragment());
        fragments.add(new NoteFragment());
        mainViewPager2 = findViewById(R.id.viewPager2);
        mainViewPager2.setAdapter(new MainAdapter(getSupportFragmentManager(), getLifecycle(), fragments));

        tl = findViewById(R.id.tabLayout);
        TabLayoutMediator tab = new TabLayoutMediator(tl, mainViewPager2, (tab1, position) -> {
            switch (position){
                case 0:
                    tab1.setText(R.string.note_zh);
                    break;
                case 1:
                    tab1.setText(R.string.todo_zh);
                    break;
            }
        });
        tab.attach();
        getNoteList();

        MainAdapter adapter = (MainAdapter) mainViewPager2.getAdapter();
        if (adapter != null) {
            NoteFragment fragment = (NoteFragment) adapter.getFragment(0);
            if (fragment != null) {
                // Call the method
                // 获取refresh

            }
        }
    }

    public void getNoteList() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int user_id = sharedPreferences.getInt("user_id", -1);
        editor.apply();
        if (user_id == -1) {
            //未登录
            return;
        }
        //获取笔记列表
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, GET_NOTE_LIST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_NOTE_LIST) {
            if (grantResults.length > 0 && grantResults[0] == 0) {
                // Permission granted
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int user_id = sharedPreferences.getInt("user_id", -1);
                editor.apply();

                // okhttp GET
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder().url(API.API_root + "/notes/" + user_id).get().build();
                try {
                    client.newCall(request).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("error", e.toString());
                                    Toast.makeText(MainActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    assert response.body() != null;
                                                    String responseData = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(responseData);
                                                    String array = jsonObject.getString("array");
                                                    Gson gson = new Gson();
                                                    Type type = new TypeToken<List<Note>>() {
                                                    }.getType();
                                                    noteList = gson.fromJson(array, type);
                                                    //Decode
                                                    for (Note note : noteList) {
                                                        Log.d("note", "source_title: " + note.getTitle());
                                                        Log.d("note", "source_first_line: " + note.getFirst_line());
                                                        note.setTitle(Uri.decode(note.getTitle()));
                                                        Log.d("note", "decoded_: " + note.getTitle());
                                                        note.setFirst_line(Uri.decode(note.getFirst_line()));
                                                        Log.d("note", "decoded_first_line: " + note.getFirst_line());

                                                    }
                                                    Log.d("note", "initMainNoteList: " + noteList.size());
                                                    MainAdapter adapter = (MainAdapter) mainViewPager2.getAdapter();
                                                    if (adapter != null) {
                                                        NoteFragment fragment = (NoteFragment) adapter.getFragment(0);
                                                        if (fragment != null) {
                                                            // Call the method
                                                            fragment.initNoteList(noteList);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    Log.e("error", e.toString());
                                                    Toast.makeText(MainActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "获取信息失败:" + response.code(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                } catch (Exception e) {
                    Log.e("error", e.toString());
                    Toast.makeText(this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "无法访问网络", Toast.LENGTH_SHORT).show();
            }
        }
    }
}