package com.example.bouquetbliss.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 80; // JPEG compression quality

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap from URI: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap resizeBitmap(Bitmap originalBitmap, int maxSize) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Calculate new dimensions
        float ratio = Math.min(
            (float) maxSize / width,
            (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    public static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
        return baos.toByteArray();
    }

    public static byte[] processImageForUpload(Context context, Uri uri) {
        try {
            Bitmap originalBitmap = getBitmapFromUri(context, uri);
            if (originalBitmap == null) {
                return null;
            }

            Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SIZE);
            return compressBitmap(resizedBitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            return null;
        }
    }
}
