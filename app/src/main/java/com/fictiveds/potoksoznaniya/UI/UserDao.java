package com.fictiveds.potoksoznaniya.UI;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    // Получение пользователя по ID
    @Query("SELECT * FROM users WHERE userId = :userId")
    User getUserById(String userId);

    // Вставка или обновление пользователя
    // OnConflictStrategy.REPLACE говорит Room заменять конфликтующие данные
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    // Удаление пользователя
    @Delete
    void deleteUser(User user);

    // Получение всех пользователей
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}
