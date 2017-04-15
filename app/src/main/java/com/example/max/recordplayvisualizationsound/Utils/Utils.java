package com.example.max.recordplayvisualizationsound.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Max on 15.04.2017.
 */

public class Utils {
    public static boolean checkPermission(Context gottenApplicationContext) {
        int result = ContextCompat.checkSelfPermission(gottenApplicationContext,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(gottenApplicationContext,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}
