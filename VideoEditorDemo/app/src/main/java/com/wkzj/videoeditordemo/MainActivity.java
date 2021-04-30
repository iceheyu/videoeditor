package com.wkzj.videoeditordemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import net.heyspace.videoeditor.EditUtil;
import net.heyspace.videoeditor.structs.VideoFragment;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testEditor();
            }
        });
    }

    private void testEditor() {
        XXPermissions.with(this)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {

                        EditUtil editUtil = new EditUtil();
                        editUtil.setOnEditingListener(null);
                        String videoPath = "/mnt/sdcard/drama.mp4";
                        String savePath = "/mnt/sdcard/drama1.mp4";
                        VideoFragment fragment = new VideoFragment(videoPath, 3_000_000, 9_000_000);
                        try {
                            editUtil.slice(fragment, savePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                    }
                });
    }
}
