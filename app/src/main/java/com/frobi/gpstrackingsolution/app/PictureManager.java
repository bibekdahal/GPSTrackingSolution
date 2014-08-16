package com.frobi.gpstrackingsolution.app;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PictureManager {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOAD_PHOTO = 2;
    static final String IMAGE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GPSTrackerPhotos/";

    private static String m_lastImageFileName = "";
    public static boolean TakePicture(Activity activity) {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = IMAGE_DIRECTORY + imageFileName + ".jpg";
        File newFile = new File(file);
        try {
            new File(newFile.getParent()).mkdirs();
            newFile.createNewFile();
        } catch (IOException e) {
            return false;
        }

        Uri outputFileUri = Uri.fromFile(newFile);
        m_lastImageFileName = file;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        activity.startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);

        return true;
    }

    public static void SelectImage(Activity activity) {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(i, REQUEST_LOAD_PHOTO);
    }

    public static String Result(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOAD_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage==null) return "";
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor==null) return "";
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                File src = new File(picturePath);
                File dst = new File(IMAGE_DIRECTORY+src.getName());
                int i=0;
                while (dst.exists())
                {dst = new File(IMAGE_DIRECTORY+src.getName()+i); i++;}

                new File(dst.getParent()).mkdirs();
                try {
                    Copy(src, dst);
                    return src.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }

        if (m_lastImageFileName.equals("")) return "";
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode != Activity.RESULT_OK) {
                new File(m_lastImageFileName).delete();
                m_lastImageFileName = "";
            }
            else return m_lastImageFileName;
        }
        return "";
    }

    public static void Copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static List<File> GetAllImages() {
        return GetAllFiles(new File(IMAGE_DIRECTORY));
    }
    public static List<File> GetAllFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        if (files==null) return null;
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(GetAllFiles(file));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }

    public static void DeleteAllImages() {
        List<File> files = GetAllImages();
        for (File file:files) {
            if (file.exists())
                file.delete();
        }
    }
}
