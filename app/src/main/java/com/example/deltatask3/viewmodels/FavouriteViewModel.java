package com.example.deltatask3.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.database.FavouritesRepository;

import java.util.List;

public class FavouriteViewModel extends AndroidViewModel {

    private FavouritesRepository repository;
    private LiveData<List<Favourite>> allFavourites;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        repository=new FavouritesRepository(application);
        allFavourites=repository.getAllFavourites();
    }

    public void insert(Favourite favourite){
        repository.insert(favourite);
    }

    public void delete(Favourite favourite){
        repository.delete(favourite);
    }

    public void deleteAllFavourites(){
        repository.deleteAllFavourites();
    }

    public LiveData<List<Favourite>> getAllFavourites() {
        return allFavourites;
    }
}
