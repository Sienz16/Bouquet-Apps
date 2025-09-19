package com.example.bouquetbliss.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to automatically translate UI elements in an Activity
 */
public class UITranslator {
    
    private TranslationManager translationManager;
    private Activity activity;
    
    public UITranslator(Activity activity) {
        this.activity = activity;
        this.translationManager = TranslationManager.getInstance(activity);
    }
    
    /**
     * Automatically translate all TextView and Button elements in the activity
     */
    public void translateActivity() {
        View rootView = activity.findViewById(android.R.id.content);
        translateViewGroup((ViewGroup) rootView);
    }
    
    /**
     * Translate a specific ViewGroup and all its children
     */
    public void translateViewGroup(ViewGroup viewGroup) {
        if (viewGroup == null) return;
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof ViewGroup) {
                translateViewGroup((ViewGroup) child);
            } else if (child instanceof TextView) {
                translateTextView((TextView) child);
            } else if (child instanceof Button) {
                translateButton((Button) child);
            } else if (child instanceof MaterialButton) {
                translateMaterialButton((MaterialButton) child);
            }
        }
    }
    
    /**
     * Translate a TextView if it contains English text
     */
    private void translateTextView(TextView textView) {
        if (textView == null) return;
        
        CharSequence text = textView.getText();
        if (text != null && !text.toString().trim().isEmpty()) {
            String textString = text.toString();
            
            // Skip if text looks like it's already translated or contains special characters
            if (shouldTranslate(textString)) {
                translationManager.translateAndSetText(textView, textString);
            }
        }
    }
    
    /**
     * Translate a Button if it contains English text
     */
    private void translateButton(Button button) {
        if (button == null) return;
        
        CharSequence text = button.getText();
        if (text != null && !text.toString().trim().isEmpty()) {
            String textString = text.toString();
            
            if (shouldTranslate(textString)) {
                translationManager.translateText(textString, new TranslationManager.TranslationCallback() {
                    @Override
                    public void onTranslated(String translatedText) {
                        button.setText(translatedText);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Keep original text on error
                    }
                });
            }
        }
    }
    
    /**
     * Translate a MaterialButton if it contains English text
     */
    private void translateMaterialButton(MaterialButton button) {
        if (button == null) return;
        
        CharSequence text = button.getText();
        if (text != null && !text.toString().trim().isEmpty()) {
            String textString = text.toString();
            
            if (shouldTranslate(textString)) {
                translationManager.translateText(textString, new TranslationManager.TranslationCallback() {
                    @Override
                    public void onTranslated(String translatedText) {
                        button.setText(translatedText);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Keep original text on error
                    }
                });
            }
        }
    }
    
    /**
     * Determine if text should be translated
     */
    private boolean shouldTranslate(String text) {
        // Don't translate if English is selected
        if (translationManager.isEnglishSelected()) {
            return false;
        }
        
        // Don't translate empty or very short text
        if (text.trim().length() < 2) {
            return false;
        }
        
        // Don't translate numbers, dates, prices, emails, etc.
        if (text.matches(".*\\d.*") && (
            text.contains("RM") || 
            text.contains("$") || 
            text.contains("@") ||
            text.contains("/") ||
            text.contains(":") ||
            text.matches(".*\\d{2,}.*") // Contains 2+ digit numbers
        )) {
            return false;
        }
        
        // Don't translate single characters or symbols
        if (text.length() == 1) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Manually translate specific TextViews with custom text
     */
    public void translateTextViews(Map<TextView, String> textViewMap) {
        translationManager.translateTexts(textViewMap);
    }
    
    /**
     * Translate common UI elements by their typical text content
     */
    public void translateCommonElements() {
        Map<String, String> commonTranslations = new HashMap<>();
        
        // Navigation
        commonTranslations.put("Home", "Laman Utama");
        commonTranslations.put("Browse", "Layari");
        commonTranslations.put("Orders", "Pesanan");
        commonTranslations.put("Profile", "Profil");
        commonTranslations.put("Favorites", "Kegemaran");
        
        // Actions
        commonTranslations.put("Save", "Simpan");
        commonTranslations.put("Cancel", "Batal");
        commonTranslations.put("Delete", "Padam");
        commonTranslations.put("Edit", "Edit");
        commonTranslations.put("Add", "Tambah");
        commonTranslations.put("Remove", "Buang");
        commonTranslations.put("Back", "Kembali");
        commonTranslations.put("Submit", "Hantar");
        commonTranslations.put("Confirm", "Sahkan");
        commonTranslations.put("Update", "Kemaskini");
        
        // Cache these translations
        for (Map.Entry<String, String> entry : commonTranslations.entrySet()) {
            translationManager.translateText(entry.getKey(), new TranslationManager.TranslationCallback() {
                @Override
                public void onTranslated(String translatedText) {
                    // Just cache it
                }
                
                @Override
                public void onError(String error) {
                    // Ignore errors
                }
            });
        }
    }
    
    /**
     * Quick method to translate specific text and get result via callback
     */
    public void translate(String text, TranslationManager.TranslationCallback callback) {
        translationManager.translateText(text, callback);
    }
}
