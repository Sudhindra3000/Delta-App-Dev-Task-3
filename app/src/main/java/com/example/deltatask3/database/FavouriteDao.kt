package com.example.deltatask3.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavouriteDao {
    @Insert
    fun insertFavourite(favourite: Favourite)

    @Delete
    fun deleteFavourite(favourite: Favourite)

    @Query("DELETE FROM favourites_table")
    fun deleteAllFavourites()

    @Query("SELECT * FROM favourites_table")
    fun getAllFavourites(): LiveData<List<Favourite>>
}
