package com.example.deltatask3.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FavouritesRepository {

    private FavouriteDao favouriteDao;
    private LiveData<List<Favourite>> allFavourites;

    public FavouritesRepository(Application application) {
        FavouriteDatabase database = FavouriteDatabase.getInstance(application);
        favouriteDao = database.favouriteDao();
        allFavourites = favouriteDao.getAllFavourites();
    }

    public void insert(Favourite favourite) {
        InsertAsyncTask task = new InsertAsyncTask(favouriteDao);
        task.execute(favourite);
    }

    public void delete(Favourite favourite) {
        DeleteAsyncTask task = new DeleteAsyncTask(favouriteDao);
        task.execute(favourite);
    }

    public void deleteAllFavourites() {
        DeleteAllAsyncTask task = new DeleteAllAsyncTask(favouriteDao);
        task.execute();
    }

    public LiveData<List<Favourite>> getAllFavourites() {
        return allFavourites;
    }

    private static class InsertAsyncTask extends AsyncTask<Favourite, Void, Void> {

        private FavouriteDao favouriteDao;

        public InsertAsyncTask(FavouriteDao favouriteDao) {
            this.favouriteDao = favouriteDao;
        }

        @Override
        protected Void doInBackground(Favourite... favourites) {
            favouriteDao.insertFavourite(favourites[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Favourite, Void, Void> {

        private FavouriteDao favouriteDao;

        public DeleteAsyncTask(FavouriteDao favouriteDao) {
            this.favouriteDao = favouriteDao;
        }

        @Override
        protected Void doInBackground(Favourite... favourites) {
            favouriteDao.deleteFavourite(favourites[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private FavouriteDao favouriteDao;

        public DeleteAllAsyncTask(FavouriteDao favouriteDao) {
            this.favouriteDao = favouriteDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            favouriteDao.deleteAllFavourites();
            return null;
        }
    }
}
