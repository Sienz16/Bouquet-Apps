package com.example.bouquetbliss.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.example.bouquetbliss.R;

import java.io.File;
import java.io.FileInputStream;

/**
 * Utility class for managing local image storage and loading
 * FREE alternative to Firebase Storage - no billing required!
 */
public class LocalImageManager {
    
    private static final String IMAGES_DIR = "bouquet_images";
    
    /**
     * Load a local image into an ImageView
     * @param context Application context
     * @param imagePath Local image path (format: "local://bouquet_images/filename.jpg")
     * @param imageView Target ImageView
     */
    public static void loadLocalImage(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || !imagePath.startsWith("local://")) {
            // No image available - clear any existing image
            imageView.setImageDrawable(null);
            return;
        }
        
        try {
            // Extract filename from path
            String fileName = imagePath.replace("local://bouquet_images/", "");
            
            // Get the image file
            File imagesDir = new File(context.getFilesDir(), IMAGES_DIR);
            File imageFile = new File(imagesDir, fileName);
            
            if (imageFile.exists()) {
                // Load and display the image
                FileInputStream fis = new FileInputStream(imageFile);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    // Failed to decode, clear image
                    imageView.setImageDrawable(null);
                }
            } else {
                // Image file doesn't exist, clear image
                imageView.setImageDrawable(null);
            }
            
        } catch (Exception e) {
            // Error loading image, clear image
            imageView.setImageDrawable(null);
        }
    }
    
    /**
     * Check if a local image exists
     * @param context Application context
     * @param imagePath Local image path
     * @return true if image exists
     */
    public static boolean imageExists(Context context, String imagePath) {
        if (imagePath == null || !imagePath.startsWith("local://")) {
            return false;
        }
        
        try {
            String fileName = imagePath.replace("local://bouquet_images/", "");
            File imagesDir = new File(context.getFilesDir(), IMAGES_DIR);
            File imageFile = new File(imagesDir, fileName);
            return imageFile.exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Delete a local image file
     * @param context Application context
     * @param imagePath Local image path
     * @return true if successfully deleted
     */
    public static boolean deleteLocalImage(Context context, String imagePath) {
        if (imagePath == null || !imagePath.startsWith("local://")) {
            return false;
        }
        
        try {
            String fileName = imagePath.replace("local://bouquet_images/", "");
            File imagesDir = new File(context.getFilesDir(), IMAGES_DIR);
            File imageFile = new File(imagesDir, fileName);
            
            if (imageFile.exists()) {
                return imageFile.delete();
            }
            return true; // Already doesn't exist
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the size of all stored images
     * @param context Application context
     * @return Size in bytes
     */
    public static long getTotalImageSize(Context context) {
        try {
            File imagesDir = new File(context.getFilesDir(), IMAGES_DIR);
            if (!imagesDir.exists()) {
                return 0;
            }
            
            long totalSize = 0;
            File[] files = imagesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    totalSize += file.length();
                }
            }
            return totalSize;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Clean up orphaned images (images not referenced in any bouquet)
     * Call this periodically to manage storage space
     * @param context Application context
     */
    public static void cleanupOrphanedImages(Context context) {
        // TODO: Implement cleanup logic
        // This would require checking Firestore for all bouquet image references
        // and deleting local files that are no longer referenced
    }
}
