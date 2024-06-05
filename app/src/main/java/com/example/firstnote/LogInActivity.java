package com.example.firstnote;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogInActivity extends AppCompatActivity {
    ImageButton back;
    Button submit;
    Button go_reset;
    Button go_register;
    EditText usernameEditText;
    EditText passwordEditText;

    static final int REQUEST_INTERNET = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        back = findViewById(R.id.back_button);
        submit = findViewById(R.id.submit_button);
        go_reset = findViewById(R.id.go_edit_password_button);
        go_register = findViewById(R.id.go_register_button);

        back.setOnClickListener(v -> finish());
        submit.setOnClickListener(v -> {
            // Request permissions
            ActivityCompat.requestPermissions(LogInActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
        });
        go_reset.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
        go_register.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        usernameEditText = findViewById(R.id.username_input);
        passwordEditText = findViewById(R.id.password_input);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted

                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(API.API_root + "/user/login")
                        .post(RequestBody.create(MediaType.parse("application/json"), "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                        .build();
                try {
                    client.newCall(request).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("error", e.toString());
                                    Toast.makeText(LogInActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(LogInActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                            try {
                                                assert response.body() != null;
                                                JSONObject jsonObject = new JSONObject(response.body().string());
                                                int user_id = jsonObject.getInt("user_id");

                                                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                                editor.putString("username", username);
                                                editor.putInt("user_id", user_id);
                                                editor.apply();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        finish();
                                                    }
                                                }, 1000); // 延迟1秒
                                            } catch (JSONException | IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(LogInActivity.this, "登录失败:" + response.code(), Toast.LENGTH_SHORT).show());
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
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
