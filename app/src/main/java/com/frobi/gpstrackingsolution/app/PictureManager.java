package com.frobi.gpstrackingsolution.app;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureManager {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final String IMAGE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GPSTrackerPhotos/";

    public static boolean TakePicture(Activity activity) {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = IMAGE_DIRECTORY+imageFileName+".jpg";
        File newFile = new File(file);
        try {
            new File(newFile.getParent()).mkdirs();
            newFile.createNewFile();
        } catch (IOException e) { return false; }

        Uri outputFileUri = Uri.fromFile(newFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        activity.startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);

        return true;
    }
}
