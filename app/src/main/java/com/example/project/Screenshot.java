package com.example.project;

import android.graphics.Bitmap;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class Screenshot {

    public static Bitmap takescreenshot(Mat v)
    {

        Bitmap b=Bitmap.createBitmap(v.cols(),v.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(v,b);
        return b;
    }

    public static Bitmap takescreenshotofRootView(Mat v) {
        return takescreenshot(v);
    }
}
