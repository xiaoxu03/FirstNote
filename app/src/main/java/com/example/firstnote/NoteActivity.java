package com.example.firstnote;

import static android.os.Environment.getExternalStorageDirectory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import jp.wasabeef.richeditor.RichEditor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    ImageButton insertVideoButton;
    ImageButton insertImageButton;
    ImageButton insertAudioButton;
    ImageButton getHtmlButton;
    RichEditor mEditor;
    TextView titleText;
    boolean isSync = true;
    Handler handler = new Handler();
    Runnable syncRunnable;
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
        assert title != null;
        Log.d("title", title);

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
        titleText.setText(title);

        getHtmlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String html = mEditor.getHtml();
                Log.d("html", html);
            }
        });

        // okhttp GET
        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String encodedTitle = Uri.encode(title);

        int user_id = sharedPreferences.getInt("user_id", -1);
        editor.apply();
        String url = API.API_root + "/note/"+user_id+"/"+encodedTitle;
        Log.d("get_url", url);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
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
                                            String decodedContent = Uri.decode(content);
                                            Log.d("note", decodedContent);
                                            mEditor.setHtml(decodedContent);
                                            isSync = false;
                                        }
                                    });
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            );
        } catch (Exception e) {
            Log.e("get_error", e.toString());
        }
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                // 在这里调用SyncNote方法
                if (!isSync)
                    SyncNote();
                isSync = false;

                // 再次调用postDelayed方法，实现定时任务
                handler.postDelayed(this, 1000); // 3秒后再次执行
            }
        };

        // 使用Handler的postDelayed方法来在60秒后执行Runnable
        handler.postDelayed(syncRunnable, 1000);
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

    boolean SyncNote(){
        // okhttp POST
        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
        int user_id = sharedPreferences.getInt("user_id", -1);
        String temp_title = titleText.getText().toString();
        String content = mEditor.getHtml();
        String encodedTitle = Uri.encode(temp_title);
        String encodedContent = "";
        if(content != null) {
            encodedContent = Uri.encode(content);
        }
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{\"title\":\"" + encodedTitle + "\",\"note_content\":\"" + encodedContent + "\", \"labels\":\"\"}");

        Request request = new Request.Builder()
                .url(API.API_root + "/note/"+user_id)
                .patch(requestBody)
                .build();

        try {
            client.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("upload_error", e.toString());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                Log.d("upload_success", Objects.requireNonNull(response.body()).string());
                            } else {
                                Log.e("upload_error", Objects.requireNonNull(response.body()).string());
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("upload_error", e.toString());
        }
        return true;
    }

    @SuppressLint("Recycle")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_GET:
                    if (data != null) {
                        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
                        int user_id = sharedPreferences.getInt("user_id", -1);
                        Uri uri = data.getData();
                        assert uri != null;
                        String back_name = uri.toString().substring(uri.toString().lastIndexOf(".")+1);

                        // okhttp POST
                        // Upload file to server
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", "image.jpg",
                                        RequestBody.create(MediaType.parse("image/*"), new File(Objects.requireNonNull(uri.getPath()).substring(4))))
                                .addFormDataPart("backname", back_name)
                                .build();
                        String url = API.API_root + "/file/"+user_id;
                        Log.d("upload_url", url);
                        Request request = new Request.Builder()
                                .url(API.API_root + "/file/"+user_id)
                                .post(requestBody)
                                .build();
                        try {
                            client.newCall(request).enqueue(
                                    new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e("upload_error", e.toString());
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                String resp_str = Objects.requireNonNull(response.body()).string();
                                                try {
                                                    JSONObject jsonObject = new JSONObject(resp_str);
                                                    int file_id = jsonObject.getInt("file_id");

                                                    String url = API.API_root + "/file/"+user_id+"/" + file_id + "." + back_name;
                                                    Log.d("upload_success", url);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mEditor.insertImage(url, url + "\" style=\"max-width:100%");
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.e("upload_error", Objects.requireNonNull(response.body()).string());
                                            }
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            Log.e("upload_error", e.toString());
                        }
                    } else {
                        Toast.makeText(this, "图片损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_VIDEO_GET:
                    if (data != null) {
                        Uri uri = data.getData();
                        assert uri != null;

                        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
                        int user_id = sharedPreferences.getInt("user_id", -1);
                        String back_name = uri.toString().substring(uri.toString().lastIndexOf(".")+1);
                        // okhttp POST
                        // Upload file to server
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", "video.mp4",
                                        RequestBody.create(MediaType.parse("video/*"), new File(uri.getPath().substring(4))))
                                .addFormDataPart("backname", back_name)
                                .build();

                        Request request = new Request.Builder()
                                .url(API.API_root + "/file/"+user_id)
                                .post(requestBody)
                                .build();
                        try {
                            client.newCall(request).enqueue(
                                    new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e("upload_error", e.toString());
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                                                    int file_id = jsonObject.getInt("file_id");
                                                    String url = API.API_root + "/file/"+user_id+"/"+file_id + "." + back_name;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mEditor.insertVideo(url + "\" style=\"max-width:100%");
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.e("upload_error", Objects.requireNonNull(response.body()).string());
                                            }
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            Log.e("upload_error", e.toString());
                        }
                    } else {
                        Toast.makeText(this, "视频损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_AUDIO_GET:
                    if (data != null) {
                        Uri uri = data.getData();
                        assert uri != null;

                        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
                        int user_id = sharedPreferences.getInt("user_id", -1);
                        String back_name = uri.toString().substring(uri.toString().lastIndexOf(".")+1);
                        // okhttp POST
                        // Upload file to server
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", "audio.mp3",
                                        RequestBody.create(MediaType.parse("audio/*"), new File("/storage/emulated/0/"+uri.getPath().substring(15))))
                                .addFormDataPart("backname", back_name)
                                .build();

                        Request request = new Request.Builder()
                                .url(API.API_root + "/file/"+user_id)
                                .post(requestBody)
                                .build();

                        try {
                            client.newCall(request).enqueue(
                                    new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            Log.e("upload_error", e.toString());
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                                                    int file_id = jsonObject.getInt("file_id");
                                                    String url = API.API_root + "/file/"+user_id+"/"+file_id + "." + back_name;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mEditor.insertAudio(url);
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.e("upload_error", Objects.requireNonNull(response.body()).string());
                                            }
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            Log.e("upload_error", e.toString());
                        }
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected
    void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(syncRunnable);
    }
}
