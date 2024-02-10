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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageHandler {

    private final Activity activity;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_GALLERY_REQUEST_CODE = 101;
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_GALLERY = 103;


    public interface UploadCallback {
        void onUploadSuccess(Uri downloadUri);
        void onUploadFailure(Exception exception);
    }

    public interface ImageLoadCallback {
        void onLocalImageNotAvailable();
    }

    public ImageHandler(Activity activity) {
        this.activity = activity;
    }

    public void loadProfileImageFromFirebaseStorage(String userId, CircleImageView profileImage, ImageLoadCallback callback) {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(activity).load(uri).into(profileImage);
        }).addOnFailureListener(exception -> {
            callback.onLocalImageNotAvailable();
        });
    }

    public void uploadImageToFirebaseStorage(Uri imageUri, String userId, UploadCallback callback) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");

        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                callback.onUploadSuccess(downloadUri);
                saveImageToLocal(userId, downloadUri); // Сохраняем локально
            });
        }).addOnFailureListener(exception -> {
            callback.onUploadFailure(exception);
            Toast.makeText(activity, "Upload Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void saveImageToLocal(String userId, Uri imageUri) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(userId + "_image", imageUri.toString());
        editor.apply();
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

    public void deleteProfileImageFromFirebaseStorage(String userId, DeleteCallback deleteCallback) {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profileImages/" + userId + ".jpg");
        profileImageRef.delete().addOnSuccessListener(unused -> {
            deleteCallback.onDeleteSuccess();
        }).addOnFailureListener(exception -> {
            deleteCallback.onDeleteFailure(exception);
        });
    }


}