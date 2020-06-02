package com.example.deltatask3.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Favourite.class}, version = 1)
public abstract class FavouriteDatabase extends RoomDatabase {

    private static FavouriteDatabase instance;

    public abstract FavouriteDao favouriteDao();

    public static synchronized FavouriteDatabase getInstance(Context context) {
        if (instance==null){
            instance= Room.databaseBuilder(context.getApplicationContext(),FavouriteDatabase.class,"favourite_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
