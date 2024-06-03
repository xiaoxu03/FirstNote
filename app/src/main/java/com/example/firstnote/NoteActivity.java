package com.example.firstnote;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import jp.wasabeef.richeditor.RichEditor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    ImageButton insertVideoButton;
    ImageButton insertImageButton;
    ImageButton insertAudioButton;
    ImageButton getHtmlButton;
    RichEditor mEditor;
    TextView titleText;
    String[] mPermissionList = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
    };

    static final int REQUEST_IMAGE_GET = 100;
    static final int REQUEST_VIDEO_GET = 101;
    static final int REQUEST_AUDIO_GET = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        insertImageButton = findViewById(R.id.insert_image_button);
        insertVideoButton = findViewById(R.id.insert_video_button);
        insertAudioButton = findViewById(R.id.insert_audio_button);

        mEditor = findViewById(R.id.editor);
        insertImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                ActivityCompat.requestPermissions(NoteActivity.this, mPermissionList, REQUEST_IMAGE_GET);
            }
        });

        insertVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                ActivityCompat.requestPermissions(NoteActivity.this, mPermissionList, REQUEST_VIDEO_GET);
            }
        });

        insertAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                ActivityCompat.requestPermissions(NoteActivity.this, mPermissionList, REQUEST_AUDIO_GET);
            }
        });

        getHtmlButton = findViewById(R.id.get_html);
        titleText = findViewById(R.id.title_text);

        getHtmlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String html = mEditor.getHtml();
                Log.d("html", html);
            }
        });

        // okhttp DELETE
        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        int user_id = sharedPreferences.getInt("user_id", -1);
        editor.apply();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(API.API_root + "/note/"+user_id+"/"+title)
                .get()
                .build();
        try {
            client.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("get_error", e.toString());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                try {
                                    assert response.body() != null;
                                    JSONObject jsonObject = new JSONObject(response.body().string());

                                    if (jsonObject.has("note_content")){
                                        String content = jsonObject.getString("note_content");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                titleText.setText(title);
                                                mEditor.setHtml(content);
                                            }
                                        });
                                    }

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Log.e("get_error", Objects.requireNonNull(response.body()).string());
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("get_error", e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_GET) {
            boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (readExternalStorage) {
                getImage();
            } else {
                Toast.makeText(this, "请设置必要权限", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_VIDEO_GET) {
            boolean readExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (readExternalStorage) {
                getVideo();
            } else {
                Toast.makeText(this, "请设置必要权限", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_AUDIO_GET) {
            boolean readExternalStorage = grantResults[2] == PackageManager.PERMISSION_GRANTED;
            if (readExternalStorage) {
                getAudio();
            } else {
                Toast.makeText(this, "请设置必要权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    private void getVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_GET);
    }

    private void getAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_AUDIO_GET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_GET:
                    if (data != null) {
                        Uri uri = data.getData();
                        assert uri != null;
                        mEditor.insertImage(uri.toString(), uri.toString() + "\" style=\"max-width:100%");
                    } else {
                        Toast.makeText(this, "图片损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_VIDEO_GET:
                    if (data != null) {
                        Uri uri = data.getData();
                        assert uri != null;
                        mEditor.insertVideo(uri.toString() + "\" style=\"max-width:100%", mEditor.getWidth());
                    } else {
                        Toast.makeText(this, "视频损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_AUDIO_GET:
                    if (data != null) {
                        Uri uri = data.getData();
                        assert uri != null;
                        mEditor.insertAudio(uri.toString() + "\" style=\"max-width:100%");
                    } else {
                        Toast.makeText(this, "音频损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
