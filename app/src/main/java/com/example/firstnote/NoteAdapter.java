package com.example.firstnote;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class NoteAdapter extends BaseQuickAdapter<Note, BaseViewHolder>{
    private static final int STATE_DEFAULT = 0;
    int mEditMode = STATE_DEFAULT;

    public NoteAdapter(int layoutResId, @Nullable List<Note> noteList) {
        super(layoutResId, noteList);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, Note item) {
        helper.setText(R.id.title_text, item.title);
        helper.setText(R.id.first_line_text, item.first_line);
        helper.addOnClickListener(R.id.note_item);//添加item点击事件
        helper.addOnLongClickListener(R.id.note_item);//添加item长按事件
        helper.getView(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除笔记
                int position = helper.getAdapterPosition();
                Note note = getItem(position);
                if (note != null) {
                    remove(position);
                    // okhttp DELETE
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("user", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                    int user_id = sharedPreferences.getInt("user_id", -1);
                    editor.apply();
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(API.API_root + "/note/"+user_id+"/"+item.title)
                            .delete()
                            .build();
                    try {
                        client.newCall(request).enqueue(
                                new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.e("delete_error", e.toString());
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            Log.d("delete_success", Objects.requireNonNull(response.body()).string());
                                        } else {
                                            Log.e("delete_error", Objects.requireNonNull(response.body()).string());
                                        }
                                    }
                                }
                        );
                    } catch (Exception e) {
                        Log.e("delete_error", e.toString());
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();//刷新
    }
}
