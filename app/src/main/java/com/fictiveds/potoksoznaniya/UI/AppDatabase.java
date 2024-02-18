package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Аннотация Database принимает массив сущностей и версию базы данных.
// Каждый раз, когда вы изменяете схему базы данных, вам нужно увеличивать версию.
@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Объявите абстрактные методы для каждого DAO, которые работают с базой данных.
    public abstract UserDao userDao();

    // Singleton предотвращает множественные экземпляры базы данных открываться в одно и то же время.
    private static volatile AppDatabase INSTANCE;

    // getDatabase возвращает синглтон. Он создаст базу данных при первом доступе,
    // используя Room's database builder для создания RoomDatabase объекта в контексте приложения из класса AppDatabase.
    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration() // Указывает на то, что при обнаружении несовместимости версий, Room воссоздаст таблицы
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
