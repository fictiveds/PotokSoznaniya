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


public class ImageManager {

    private final Activity activity;
    private final ImageHandler imageHandler;

    public ImageManager (Activity activity) {
        this.activity = activity;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        this.imageHandler = new ImageHandler(activity);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
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

    public void deleteProfileImageFromFirebaseStorage(String userId, ImageHandler.DeleteCallback deleteCallback) {
        imageHandler.deleteProfileImageFromFirebaseStorage(userId, new ImageHandler.DeleteCallback() {
            @Override
            public void onDeleteSuccess() {
                Toast.makeText(activity, activity.getString(R.string.profile_photo_deleted), Toast.LENGTH_SHORT).show();
                if (deleteCallback != null) {
                    deleteCallback.onDeleteSuccess();
                }
            }

            @Override
            public void onDeleteFailure(Exception exception) {
                Toast.makeText(activity, activity.getString(R.string.err_deleted) + exception.getMessage(), Toast.LENGTH_LONG).show();
                if (deleteCallback != null) {
                    deleteCallback.onDeleteFailure(exception);
                }
            }
        });
    }

    public void openCamera() {
        imageHandler.openCamera();
    }

    public void openGallery() {
        imageHandler.openGallery();
    }
}
