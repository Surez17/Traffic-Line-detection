package com.example.project;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.*;
import org.opencv.core.*;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity<intent> extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OpenCVCamera";
    private CameraBridgeViewBase camera1;

    private StorageReference sref;
    private DatabaseReference dref;
    private Uri imguri;
    private File mCascadeFile=null;
    private File cascadeDir=null;
    private CascadeClassifier cardetector=null;
    private Scalar color;
    int count=0;
    int signal=0;
    private  Mat img;
    private Point fp = new Point(0,600);
    private Point sp = new Point (2000,600);
    private String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private View main;

    private static final int CAMERA_REQUEST_ACCESS=1;
    private ProgressDialog d;
    private StorageReference file;



    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_mcs_nose);
                        cascadeDir = getDir("assets", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "cars.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    cardetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    camera1.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


 protected Mat detectCars(Mat image) {
     main=getWindow().getDecorView();
     Dialog d=new Dialog();
        MatOfRect faceDetections = new MatOfRect();
        cardetector.detectMultiScale(image, faceDetections);
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width,
                    rect.y + rect.height),
                    new Scalar(0, 255, 0));
            if(rect.y+ rect.height>560 && rect.y+ rect.height<630 && signal >29) {
                count++;
                Bitmap b = Screenshot.takescreenshotofRootView(img);
                ByteArrayOutputStream bo =new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG,20,bo);
                byte[] data = bo.toByteArray();
                String key= Date_time();
                final UploadTask uploadTask =  file.child(key).putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> image_url = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        image_url.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                    //Upload upload = new Upload(Date_time(),uri.toString());

                                    dref.child(Date_time()).setValue(uri.toString());
                            }
                        });

                    }
                });
                d.show(getSupportFragmentManager(),"Dialog");
            }


        }
     Imgproc.putText (
             image,                          // Matrix obj of the image
             "Vehicle "+count+"  ",          // Text to be added
             new Point(10, 50),               // point
             Core.FONT_HERSHEY_SIMPLEX ,      // front face
             1,                               // front sca-8666\\\le
             new Scalar(0, 255, 255),             // Scalar object for color
             4                                // Thickness
     );
        return image;
    }


    protected String Date_time()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
        String time = df.format(c.getTime());
        return time;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},0);
        }
        sref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://info-59e58.appspot.com");
        dref = FirebaseDatabase.getInstance().getReference("image");
        file = sref.child("violated_vehicles");

        d=new ProgressDialog(this);
        camera1 = findViewById(R.id.camera_view);
        camera1.setVisibility(SurfaceView.VISIBLE);
        camera1.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV Library not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, baseLoaderCallback);
        }else{
            Log.d(TAG, "OpenCV Library loaded Successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
 }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
        img.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        signal++;
        if(signal<20)
        {
            color=new Scalar(0,255,0);

        }
        else if(signal<30)
        {
            color= new Scalar(255,255,0);
        }
        else if(signal <50)
        {
            color = new Scalar(255,0,0,255);
        }
        else
        {
            signal=0;
        }

        img=inputFrame.rgba();
        img=detectCars(img);

        Imgproc.line(img,fp,sp,color,7);
        return img;
    }

}


