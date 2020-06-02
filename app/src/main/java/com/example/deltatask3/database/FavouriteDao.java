package com.example.deltatask3.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavouriteDao {

    @Insert
    void insertFavourite(Favourite favourite);

    @Delete
    void deleteFavourite(Favourite favourite);

    @Query("DELETE FROM favourites_table")
    void deleteAllFavourites();

    @Query("SELECT * FROM favourites_table")
    LiveData<List<Favourite>> getAllFavourites();
}
