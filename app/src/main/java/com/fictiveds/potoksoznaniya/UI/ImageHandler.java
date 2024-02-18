package com.fictiveds.potoksoznaniya.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.fictiveds.potoksoznaniya.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageHandler {

    private final Activity activity;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_GALLERY_REQUEST_CODE = 101;
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_GALLERY = 103;
    private final ExecutorService executorService;
    private final AppDatabase db;


    public interface UploadCallback {
        void onUploadSuccess(Uri downloadUri);
        void onUploadFailure(Exception exception);
    }

    public interface ImageLoadCallback {
        void onLocalImageNotAvailable();
    }

    public ImageHandler(Activity activity, ExecutorService executorService, AppDatabase db) {
        this.activity = activity;
        this.executorService = executorService;
        this.db = db;
    }

    public void loadProfileImageFromFirebaseStorage(String userId, CircleImageView profileImage, ImageLoadCallback callback) {
        executorService.execute(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null && user.profileImagePath != null && !user.profileImagePath.isEmpty()) {
                // Если URL изображения профиля существует в базе данных Room, загружаем его
                activity.runOnUiThread(() -> Glide.with(activity).load(user.profileImagePath).into(profileImage));
            } else {
                // Иначе загружаем изображение из Firebase Storage
                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
                profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    activity.runOnUiThread(() -> Glide.with(activity).load(uri).into(profileImage));
                    // Сохраняем URL изображения в базу данных Room
                    executorService.execute(() -> {
                        // Создаем нового пользователя или обновляем существующего
                        User userToUpdate = user != null ? user : new User(userId, null, null);
                        userToUpdate.profileImagePath = uri.toString();
                        db.userDao().insertUser(userToUpdate);
                    });
                }).addOnFailureListener(exception -> {
                    activity.runOnUiThread(() -> {
                        Glide.with(activity).load(R.drawable.ic_profile).into(profileImage);
                        if (callback != null) {
                            callback.onLocalImageNotAvailable();
                        }
                    });
                });
            }
        });
    }




    public void uploadImageToFirebaseStorage(Uri imageUri, String userId, UploadCallback callback) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Вызываем callback о том, что загрузка успешна
                callback.onUploadSuccess(downloadUri);
                // Теперь обновляем информацию о профильном изображении в базе данных Room
                executorService.execute(() -> {
                    User user = db.userDao().getUserById(userId);
                    if (user == null) {
                        user = new User(userId, null, downloadUri.toString()); // Предполагаем, что имя пользователя пока неизвестно
                    } else {
                        user.profileImagePath = downloadUri.toString();
                    }
                    db.userDao().insertUser(user);
                });
            });
        }).addOnFailureListener(exception -> {
            // Вызываем callback о том, что загрузка не удалась
            callback.onUploadFailure(exception);
            Toast.makeText(activity, "Upload Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

     public void openGallery() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_GALLERY_REQUEST_CODE);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
        }
    }

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data, CircleImageView profileImage, String userId, UploadCallback uploadCallback) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                // Получение битмапа из камеры
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(bitmap);

                // Получение Uri из Bitmap
                assert bitmap != null;
                Uri imageUri = getImageUri(activity, bitmap);
                uploadImageToFirebaseStorage(imageUri, userId, uploadCallback);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                // Получение Uri изображения из галереи
                Uri selectedImageUri = data.getData();
                profileImage.setImageURI(selectedImageUri);
                uploadImageToFirebaseStorage(selectedImageUri, userId, uploadCallback);
            }
        }
    }

    // Метод для преобразования Bitmap в Uri
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(Exception exception);
    }

    public void deleteProfileImageFromFirebaseStorage(String userId, CircleImageView profileImageView, DeleteCallback deleteCallback) {
        // Удаление изображения из Firebase Storage
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
        // Удаление не удалось, информируем пользователя
        profileImageRef.delete().addOnSuccessListener(unused -> {
            // Удаление прошло успешно, обновляем UI
            if (profileImageView != null) {
                activity.runOnUiThread(() -> profileImageView.setImageResource(R.drawable.ic_profile));
            }
            // Вызываем коллбэк об успешном удалении
            deleteCallback.onDeleteSuccess();
            // Обновляем информацию в базе данных Room
            executorService.execute(() -> {
                User user = db.userDao().getUserById(userId);
                if (user != null) {
                    user.profileImagePath = null;
                    db.userDao().insertUser(user);
                }
            });
        }).addOnFailureListener(deleteCallback::onDeleteFailure);
    }




}