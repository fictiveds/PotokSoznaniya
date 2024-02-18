package com.fictiveds.potoksoznaniya.UI;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String userId; // ID пользователя

    public String username; // Имя пользователя

    public String profileImagePath; // Путь к изображению профиля в локальной файловой системе

    public User(@NonNull String userId, String username, String profileImagePath) {
        this.userId = userId;
        this.username = username;
        this.profileImagePath = profileImagePath;
    }
}
