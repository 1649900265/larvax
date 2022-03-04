package com.zeyyo.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileUtils {
    /**
     * 获取文件行数
     * @param filePath
     * @return
     */
    public static int getFileLineNumber(String filePath) {

        try {
            File saveFile = new File(filePath);
            if (!saveFile.exists()) {
                return 0;
            }
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(filePath));
            // it will return the number of characters actually skipped
            lineNumberReader.skip(Long.MAX_VALUE);
            int lineNumber = lineNumberReader.getLineNumber();
            lineNumber++;
            lineNumberReader.close();
            return lineNumber;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String getPath(Context mContext) {
        //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // 先判断有没有权限,Environment.getExternalStorageDirectory()需要文件读取权限
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + "OSTestTowerSystem" + File.separator;
        }
        //}
        //mContext.getExternalCacheDir()不需要文件读取权限
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && mContext.getExternalCacheDir() != null)
            return mContext.getExternalCacheDir() + File.separator + "OSTestTowerSystem" + File.separator;
        else {
            return mContext.getCacheDir() + File.separator + "OSTestTowerSystem" + File.separator;
        }

    }
}
