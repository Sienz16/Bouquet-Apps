package com.example.bouquetbliss.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.HashMap;
import java.util.Map;

public class TranslationManager {
    private static final String PREF_NAME = "translation_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String LANGUAGE_ENGLISH = "en";
    private static final String LANGUAGE_MALAY = "ms";
    
    private static TranslationManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Translator englishToMalayTranslator;
    private Map<String, String> translationCache;
    private boolean isTranslatorReady = false;
    
    private TranslationManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.translationCache = new HashMap<>();
        initializeTranslator();
    }
    
    public static synchronized TranslationManager getInstance(Context context) {
        if (instance == null) {
            instance = new TranslationManager(context);
        }
        return instance;
    }
    
    private void initializeTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.MALAY)
                .build();
        
        englishToMalayTranslator = Translation.getClient(options);
        
        // Download model if needed
        englishToMalayTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        isTranslatorReady = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle download failure
                        Toast.makeText(context, "Failed to download translation model", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    public void setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
    
    public String getCurrentLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }
    
    public boolean isMalaySelected() {
        return LANGUAGE_MALAY.equals(getCurrentLanguage());
    }
    
    public boolean isEnglishSelected() {
        return LANGUAGE_ENGLISH.equals(getCurrentLanguage());
    }
    
    // Interface for translation callback
    public interface TranslationCallback {
        void onTranslated(String translatedText);
        void onError(String error);
    }
    
    // Translate text with callback
    public void translateText(String englishText, TranslationCallback callback) {
        if (isEnglishSelected()) {
            // Return original text if English is selected
            callback.onTranslated(englishText);
            return;
        }
        
        // Check cache first
        if (translationCache.containsKey(englishText)) {
            callback.onTranslated(translationCache.get(englishText));
            return;
        }
        
        if (!isTranslatorReady) {
            callback.onError("Translator not ready");
            return;
        }
        
        englishToMalayTranslator.translate(englishText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        // Cache the translation
                        translationCache.put(englishText, translatedText);
                        callback.onTranslated(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }
    
    // Convenience method to translate and set TextView
    public void translateAndSetText(TextView textView, String englishText) {
        translateText(englishText, new TranslationCallback() {
            @Override
            public void onTranslated(String translatedText) {
                textView.setText(translatedText);
            }
            
            @Override
            public void onError(String error) {
                // Fallback to original text on error
                textView.setText(englishText);
            }
        });
    }
    
    // Batch translate multiple texts
    public void translateTexts(Map<TextView, String> textViewMap) {
        for (Map.Entry<TextView, String> entry : textViewMap.entrySet()) {
            translateAndSetText(entry.getKey(), entry.getValue());
        }
    }
    
    // Pre-translate common phrases to cache them
    public void preloadCommonTranslations() {
        String[] commonPhrases = {
            // App navigation
            "Home", "Browse", "Orders", "Profile", "Favorites",
            
            // Common actions
            "Save", "Cancel", "Delete", "Edit", "Add", "Remove", "Back",
            "Submit", "Confirm", "Update", "Search", "Filter",
            
            // Order related
            "Order", "Bouquet", "Price", "Quantity", "Total", "Status",
            "Pending", "Approved", "Rejected", "Completed", "In Progress",
            
            // User interface
            "Name", "Email", "Phone", "Address", "City", "State", "Postal Code",
            "Login", "Register", "Logout", "Settings", "Language",
            
            // Messages
            "Success", "Error", "Loading", "Please wait", "Try again",
            "No items found", "Something went wrong"
        };
        
        for (String phrase : commonPhrases) {
            translateText(phrase, new TranslationCallback() {
                @Override
                public void onTranslated(String translatedText) {
                    // Just cache it, no action needed
                }
                
                @Override
                public void onError(String error) {
                    // Ignore errors for preloading
                }
            });
        }
    }
    
    // Clean up resources
    public void cleanup() {
        if (englishToMalayTranslator != null) {
            englishToMalayTranslator.close();
        }
    }
    
    // Language switching methods
    public void switchToEnglish() {
        setLanguage(LANGUAGE_ENGLISH);
    }
    
    public void switchToMalay() {
        setLanguage(LANGUAGE_MALAY);
    }
    
    public void toggleLanguage() {
        if (isMalaySelected()) {
            switchToEnglish();
        } else {
            switchToMalay();
        }
    }
    
    // Get language display names
    public String getCurrentLanguageDisplayName() {
        if (isMalaySelected()) {
            return "Bahasa Melayu";
        } else {
            return "English";
        }
    }
    
    public String[] getAvailableLanguages() {
        return new String[]{"English", "Bahasa Melayu"};
    }
    
    public String[] getAvailableLanguageCodes() {
        return new String[]{LANGUAGE_ENGLISH, LANGUAGE_MALAY};
    }
}
