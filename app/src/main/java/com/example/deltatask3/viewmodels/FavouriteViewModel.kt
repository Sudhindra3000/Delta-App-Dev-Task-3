package com.example.deltatask3.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.database.FavouritesRepository

class FavouriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FavouritesRepository = FavouritesRepository(application)
    val allFavourites: LiveData<List<Favourite>> = repository.allFavourites
    fun insert(favourite: Favourite?) = repository.insert(favourite)

    fun delete(favourite: Favourite?) = repository.delete(favourite)

    fun deleteAllFavourites() = repository.deleteAllFavourites()
}
