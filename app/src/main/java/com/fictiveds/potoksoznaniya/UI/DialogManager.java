package com.fictiveds.potoksoznaniya.UI;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fictiveds.potoksoznaniya.R;
import com.google.android.material.button.MaterialButton;
import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DialogManager {

    private final Activity activity;
    private final FirebaseAuth mAuth;
    private final ImageHandler imageHandler;
    private final CircleImageView profileImage;

    public DialogManager(Activity activity, ImageHandler imageHandler, CircleImageView profileImage) {
        this.activity = activity;
        this.imageHandler = imageHandler;
        this.mAuth = FirebaseAuth.getInstance();
        this.profileImage = profileImage;
    }

    public void showEditProfileImageDialog() {
        String[] options = {activity.getString(R.string.take_photo), activity.getString(R.string.download_from_gallery), activity.getString(R.string.del_photo)};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.edit_prof_pic));
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Сделать фото
                    imageHandler.openCamera();
                    break;
                case 1: // Загрузить из галереи
                    imageHandler.openGallery();
                    break;
                case 2: // Удалить фото
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        imageHandler.deleteProfileImageFromFirebaseStorage(user.getUid(), profileImage, new ImageHandler.DeleteCallback() {
                            @Override
                            public void onDeleteSuccess() {
                                activity.runOnUiThread(() -> {
                                    if (profileImage != null) {
                                        profileImage.setImageResource(R.drawable.ic_profile); // Устанавливаем изображение по умолчанию
                                    }
                                    Toast.makeText(activity, activity.getString(R.string.profile_photo_deleted), Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onDeleteFailure(Exception exception) {
                                // Обработка ошибки удаления
                                activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.err_deleted) + exception.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                    break;
            }
        });
        builder.show();
    }




    public void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText etOldPassword = view.findViewById(R.id.etOldPasswordDialog);
        EditText etNewPassword = view.findViewById(R.id.etNewPasswordDialog);
        Button btnConfirm = view.findViewById(R.id.btnConfirmPasswordChange);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelPasswordChange);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();

            if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                if (oldPassword.equals(newPassword)) {
                    Toast.makeText(activity, activity.getString(R.string.new_pass_must), Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(activity, activity.getString(R.string.pass_ok), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(activity, activity.getString(R.string.pass_err), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.inc_old_pass), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.err_field_empty), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}
