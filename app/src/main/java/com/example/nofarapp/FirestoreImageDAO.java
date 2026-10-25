package com.example.nofarapp;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirestoreImageDAO {
    private static final String COLLECTION_NAME = "base64_images";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // שמירת תמונת Base64 במסמך ב-Firestore
    public void saveToFirestore(String fileName, String base64, IFirestoreImageCallback<Void> callback) {
        if (fileName == null || fileName.trim().isEmpty() || base64 == null || base64.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("שם קובץ או נתוני תמונה חסרים"));
            return;
        }
        Map<String, Object> image = new HashMap<>();
        image.put("fileName", fileName);
        image.put("encoding", "BASE64");
        image.put("data", base64);
        image.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_NAME).document(fileName).set(image)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // קריאת תמונת Base64 מ-Firestore לפי שם קובץ
    public void readFromFirestore(String fileName, IFirestoreImageCallback<String> callback) {
        if (fileName == null || fileName.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("שם הקובץ חסר"));
            return;
        }
        db.collection(COLLECTION_NAME).document(fileName).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onFailure(new IllegalArgumentException("לא נמצאה תמונה בשם " + fileName));
                        return;
                    }
                    String base64 = snapshot.getString("data");
                    if (base64 == null || base64.isEmpty()) {
                        callback.onFailure(new IllegalArgumentException("נתוני התמונה חסרים ב-Firestore"));
                        return;
                    }
                    callback.onSuccess(base64);
                })
                .addOnFailureListener(callback::onFailure);
    }
}