package com.example.bouquetbliss.repository;

import com.example.bouquetbliss.models.Favorite;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {
    private static final String COLLECTION_NAME = "favorites";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FavoriteRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface FavoriteCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface FavoritesListCallback {
        void onSuccess(List<String> bouquetIds);
        void onError(String error);
    }

    public interface FavoriteStatusCallback {
        void onResult(boolean isFavorite);
        void onError(String error);
    }

    // Add bouquet to favorites
    public void addToFavorites(String bouquetId, FavoriteCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Check if already favorited
        checkIfFavorite(bouquetId, new FavoriteStatusCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                if (isFavorite) {
                    callback.onError("Already in favorites");
                    return;
                }

                // Add to favorites
                Favorite favorite = new Favorite(userId, bouquetId);
                db.collection(COLLECTION_NAME)
                        .add(favorite)
                        .addOnSuccessListener(documentReference -> {
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            callback.onError("Failed to add to favorites: " + e.getMessage());
                        });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Remove bouquet from favorites
    public void removeFromFavorites(String bouquetId, FavoriteCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("bouquetId", bouquetId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // Delete the favorite document
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                            db.collection(COLLECTION_NAME)
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                                    .addOnFailureListener(e -> callback.onError("Failed to remove from favorites: " + e.getMessage()));
                        } else {
                            callback.onError("Not in favorites");
                        }
                    } else {
                        callback.onError("Failed to check favorites: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    // Check if a bouquet is in favorites
    public void checkIfFavorite(String bouquetId, FavoriteStatusCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("bouquetId", bouquetId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        callback.onResult(result != null && !result.isEmpty());
                    } else {
                        callback.onError("Failed to check favorite status: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    // Get all favorite bouquet IDs for current user
    public void getUserFavorites(FavoritesListCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> bouquetIds = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                Favorite favorite = document.toObject(Favorite.class);
                                bouquetIds.add(favorite.getBouquetId());
                            }
                        }
                        callback.onSuccess(bouquetIds);
                    } else {
                        callback.onError("Failed to load favorites: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    // Toggle favorite status
    public void toggleFavorite(String bouquetId, FavoriteCallback callback) {
        checkIfFavorite(bouquetId, new FavoriteStatusCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                if (isFavorite) {
                    removeFromFavorites(bouquetId, callback);
                } else {
                    addToFavorites(bouquetId, callback);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
