package com.example.firstnote;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {
    ImageButton insertVideoButton;
    ImageButton insertImageButton;
    ImageButton insertAudioButton;
    ImageButton getHtmlButton;
    RichEditor mEditor;

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

        getHtmlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String html = mEditor.getHtml();
                Log.d("html", html);
            }
        });
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
