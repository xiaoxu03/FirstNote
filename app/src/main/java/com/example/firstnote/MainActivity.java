package com.example.firstnote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ViewPager2 mainViewPager2;
    TabLayout tl;

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
    }
}