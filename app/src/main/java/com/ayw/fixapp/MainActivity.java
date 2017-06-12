package com.ayw.fixapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void function(View view) {
        String value = new BugClass().getNumber();
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new BugClass().getNumber();
//            }
//        }).start();
    }

    public void fixBug(View view) {
        String fileName = "patch.dex";
        File dexDir = getDir(FixDexUtil.DEX_DIR, Context.MODE_PRIVATE);
        String dexFileName = dexDir.getAbsolutePath() + File.separator + fileName;
        File dexFile = new File(dexFileName);
        if (!dexFile.exists()) {
            copyFile(fileName, dexFile);
        }
        FixDexUtil.fix(this);
    }

    private void copyFile(String fileName, File dexFile) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = getAssets().open(fileName);
            os = new FileOutputStream(dexFile);
            byte[] buff = new byte[1024];
            int len ;
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(is);
            closeStream(os);
        }
    }

    private void closeStream(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
