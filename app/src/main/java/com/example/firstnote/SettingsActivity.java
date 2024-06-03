package com.example.firstnote;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {
    ImageButton back;
    Button log_in;

    static final int APPLY_CHANGE = 200;
    static final int GET_USER_INFO = 201;
    ImageView avatar;
    EditText nickname_edit;
    EditText signature_edit;
    EditText avatar_edit;
    Button apply_button;

    boolean is_login = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = findViewById(R.id.back_button);
        log_in = findViewById(R.id.s_go_log_in_button);
        apply_button = findViewById(R.id.apply_button);
        avatar = findViewById(R.id.avatar_image);

        nickname_edit = findViewById(R.id.nickname_input);
        signature_edit = findViewById(R.id.signature_input);
        avatar_edit = findViewById(R.id.avatar_input);

        apply_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request permissions
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.INTERNET}, APPLY_CHANGE);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        checkLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!is_login)
            checkLogin();
    }

    void checkLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Check if user is logged in
        if (sharedPreferences.getInt("user_id", -1) == -1) {
            log_in.setText("登录");
            findViewById(R.id.user_layout).setVisibility(View.GONE);
            findViewById(R.id.login_text).setVisibility(View.VISIBLE);
            is_login = false;
        } else {
            log_in.setText("注销");
            findViewById(R.id.user_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.login_text).setVisibility(View.GONE);
            is_login = true;
            log_in.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.clear();
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, "已注销", Toast.LENGTH_SHORT).show();
                    log_in.setText("登录");
                    findViewById(R.id.user_layout).setVisibility(View.GONE);
                    findViewById(R.id.login_text).setVisibility(View.VISIBLE);
                    is_login = false;
                    log_in.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.INTERNET}, GET_USER_INFO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == APPLY_CHANGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // get user info
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String nickname = nickname_edit.getText().toString();
                String avatar_url = avatar_edit.getText().toString();
                String signature = signature_edit.getText().toString();
                editor.apply();

                int user_id = sharedPreferences.getInt("user_id", 0);

                // okhttp PATCH
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(API.API_root + "/user")
                        .patch(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), "{\"nickname\":\"" + nickname + "\",\"avatar_url\":\"" + avatar_url + "\",\"signature\":\"" + signature + "\",\"user_id\":" + user_id + "}"))
                        .build();
                try {
                    client.newCall(request).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("error", e.toString());
                                    Toast.makeText(SettingsActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Glide.with(SettingsActivity.this).load(avatar_url).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatar);
                                                Toast.makeText(SettingsActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SettingsActivity.this, "修改失败:" + response.code(), Toast.LENGTH_SHORT).show();
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
        else if (requestCode == GET_USER_INFO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int user_id = sharedPreferences.getInt("user_id", -1);
                editor.apply();

                // okhttp GET
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder().url(API.API_root + "/user/" + user_id).get().build();
                try {
                    client.newCall(request).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("error", e.toString());
                                    Toast.makeText(SettingsActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    assert response.body() != null;
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    String nickname = jsonObject.getString("nickname");
                                                    String avatar_url = jsonObject.getString("avatar_url");
                                                    String signature = jsonObject.getString("signature");

                                                    nickname_edit.setText(nickname);
                                                    avatar_edit.setText(avatar_url);
                                                    signature_edit.setText(signature);

                                                    Glide.with(SettingsActivity.this).load(avatar_url).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatar);
                                                } catch (Exception e) {
                                                    Log.e("error", e.toString());
                                                    Toast.makeText(SettingsActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SettingsActivity.this, "获取信息失败:" + response.code(), Toast.LENGTH_SHORT).show();
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
