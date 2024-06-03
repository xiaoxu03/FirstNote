package com.example.firstnote;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.xuexiang.xui.widget.edittext.PasswordEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity{
    Button submit;
    ImageButton back;
    EditText passwordEditText;
    EditText usernameEditText;

    static final int REQUEST_INTERNET = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        submit = findViewById(R.id.submit_button);
        back = findViewById(R.id.back_button);

        back.setOnClickListener(v -> finish());
        submit.setOnClickListener(v -> {
            // Request permissions
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
        });
        passwordEditText = (EditText) findViewById(R.id.password_input);
        usernameEditText = (EditText) findViewById(R.id.username_input);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted

                String username = usernameEditText.getText().toString();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString();

                JSONObject json = new JSONObject();
                try {
                    json.put("username", username);
                    json.put("password", password);
                } catch (JSONException e) {
                    Log.e("error", e.toString());
                    Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
                Request request = new Request.Builder()
                        .post(body)
                        .url(API.API_root + "/user/register")
                        .build();
                Log.d("request", json.toString());
                try {
                    client.newCall(request).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("error", e.toString());
                                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        finish();
                                                    }
                                                }, 1000); // 延迟1秒
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this, "注册失败:" + response.code(), Toast.LENGTH_SHORT).show();
                                                Log.d("request", response.toString());
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
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
