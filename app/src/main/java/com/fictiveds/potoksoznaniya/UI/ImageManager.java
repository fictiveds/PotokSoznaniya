package com.fictiveds.potoksoznaniya.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fictiveds.potoksoznaniya.R;
import com.fictiveds.potoksoznaniya.UI.ImageHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.concurrent.ExecutorService;
import androidx.room.Room;


public class ImageManager {

    private final Activity activity;
    private final ImageHandler imageHandler;
    private final ExecutorService executorService; // Добавлен ExecutorService
    private final AppDatabase db;

    public ImageManager(Activity activity, ExecutorService executorService, AppDatabase db) {
        this.activity = activity;
        this.executorService = executorService; // Сохраняем ExecutorService
        this.db = db; // Сохраняем базу данных Room
        this.imageHandler = new ImageHandler(activity, executorService, db); // Передаем зависимости в ImageHandler
        // FirebaseAuth и StorageReference можно использовать напрямую в методах, если они нужны
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data, CircleImageView profileImage, String userId, ImageHandler.UploadCallback uploadCallback) {
        imageHandler.handleActivityResult(requestCode, resultCode, data, profileImage, userId, new ImageHandler.UploadCallback() {
            @Override
            public void onUploadSuccess(Uri downloadUri) {
                Glide.with(activity)
                     .load(downloadUri)
                     .diskCacheStrategy(DiskCacheStrategy.ALL)
                     .into(profileImage);
                if (uploadCallback != null) {
                    uploadCallback.onUploadSuccess(downloadUri);
                }
            }

            @Override
            public void onUploadFailure(Exception exception) {
                Toast.makeText(activity, activity.getString(R.string.upload_failed_) + exception.getMessage(), Toast.LENGTH_LONG).show();
                if (uploadCallback != null) {
                    uploadCallback.onUploadFailure(exception);
                }
            }
        });
    }

    public void loadProfileImageFromFirebaseStorage(String userId, CircleImageView profileImage, ImageHandler.ImageLoadCallback imageLoadCallback) {
        imageHandler.loadProfileImageFromFirebaseStorage(userId, profileImage, new ImageHandler.ImageLoadCallback() {
            @Override
            public void onLocalImageNotAvailable() {
                Glide.with(activity)
                        .load(R.drawable.ic_profile)
                        .into(profileImage);
                if (imageLoadCallback != null) {
                    imageLoadCallback.onLocalImageNotAvailable();
                }
            }
        });
    }

 /*   public void deleteProfileImageFromFirebaseStorage(String userId, ImageHandler.DeleteCallback deleteCallback) {
        // Удаление изображения из Firebase Storage
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
        profileImageRef.delete().addOnSuccessListener(unused -> {
            // Если изображение успешно удалено, показываем соответствующее сообщение
            Toast.makeText(activity, activity.getString(R.string.profile_photo_deleted), Toast.LENGTH_SHORT).show();
            // Вызываем коллбэк об успешном удалении
            if (deleteCallback != null) {
                deleteCallback.onDeleteSuccess();
            }

            // Теперь обновляем информацию в базе данных Room в фоновом потоке
            executorService.execute(() -> {
                User user = db.userDao().getUserById(userId);
                if (user != null) {
                    // Обновляем информацию о пути к изображению на null
                    user.profileImagePath = null;
                    // Вставляем обновленные данные пользователя в базу данных
                    db.userDao().insertUser(user);
                }
            });
        }).addOnFailureListener(exception -> {
            // Если удаление не удалось, показываем ошибку
            Toast.makeText(activity, activity.getString(R.string.err_deleted) + exception.getMessage(), Toast.LENGTH_LONG).show();
            // Вызываем коллбэк об ошибке удаления
            if (deleteCallback != null) {
                deleteCallback.onDeleteFailure(exception);
            }
        });
    }*/


    public void openCamera() {
        imageHandler.openCamera();
    }

    public void openGallery() {
        imageHandler.openGallery();
    }
}
