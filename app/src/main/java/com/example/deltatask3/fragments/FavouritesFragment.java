package com.example.deltatask3.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.deltatask3.activities.PokemonDetailsActivity;
import com.example.deltatask3.adapters.FavouriteAdapter;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FragmentFavouritesBinding;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;
import com.example.deltatask3.viewmodels.FavouriteViewModel;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class FavouritesFragment extends Fragment {

    private static final String TAG = "FavouritesFragment";
    private AppViewModel appViewModel;
    private FragmentFavouritesBinding binding;
    private FavouriteViewModel favouriteViewModel;
    private ArrayList<Favourite> searchedFavourites;
    private FavouriteAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SearchView searchView;
    private boolean searching = false;


    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Favourites");
        favouriteViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavouriteViewModel.class);
        searchedFavourites = new ArrayList<>();

        buildRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favouriteViewModel.getAllFavourites().observe(getViewLifecycleOwner(), new Observer<List<Favourite>>() {
            @Override
            public void onChanged(List<Favourite> favourites) {
                if (favourites.isEmpty()) {
                    binding.tvFDescription.setVisibility(View.VISIBLE);
                    binding.favourites.setVisibility(View.INVISIBLE);
                    setHasOptionsMenu(false);
                } else {
                    binding.tvFDescription.setVisibility(View.INVISIBLE);
                    binding.favourites.setVisibility(View.VISIBLE);
                    setHasOptionsMenu(true);
                }
                adapter.setFavourites(favourites);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void buildRecyclerView() {
        binding.favourites.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(requireContext());
        adapter = new FavouriteAdapter();
        adapter.setListener(new FavouriteAdapter.FavouriteListener() {
            @Override
            public void onItemClicked(int pos, ImageView pokemon, TextView name) {
                showDetails(adapter.getFavouriteAt(pos).getPokemon(), pokemon, name);
            }

            @Override
            public void onShareClicked(int pos, ImageView imageView) {
                sharePokemon(pos, imageView);
            }
        });

        binding.favourites.setLayoutManager(layoutManager);
        binding.favourites.setAdapter(adapter);
    }

    private void sharePokemon(int pos, ImageView imageView) {
        Pokemon pokemon = adapter.getFavouriteAt(pos).getPokemon();
        Bitmap bitmap = getBitmapFromView(imageView);
        try {
            File file = new File(requireActivity().getExternalCacheDir(), firstLetterToUppercase(pokemon.getName()) + ".png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            file.setReadable(true, false);
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getPokemonDetailsAsString(pokemon));
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share Pokémon via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPokemonDetailsAsString(Pokemon pokemon) {
        String result = "ID : " + pokemon.getId() + "\n" +
                "Pokémon : " + firstLetterToUppercase(pokemon.getName());
        return result;
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void showDetails(Pokemon pokemon, ImageView pokemonIv, TextView nameIv) {
        if (pokemon.getId() != 0 && pokemon.getSprites() != null) {
            Intent intent = new Intent(requireActivity(), PokemonDetailsActivity.class);
            Gson gson = new Gson();
            String pokemonJson = gson.toJson(pokemon);
            intent.putExtra("pokemonJson", pokemonJson);
            Pair<View, String> imagePair = new Pair<>(pokemonIv, "pokemonImg");
            Pair<View, String> namePair = new Pair<>(nameIv, "pokemonName");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair);
            startActivity(intent, options.toBundle());
        }
    }

    private void searchFavouritesByName(String name) {
        searchedFavourites.clear();

        for (Favourite favourite : favouriteViewModel.getAllFavourites().getValue()) {
            if (favourite.getPokemon().getName().trim().contains(name))
                searchedFavourites.add(new Favourite(favourite.getPokemon()));
        }

        adapter.setFavourites(searchedFavourites);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.favourites_menu, menu);
        MenuItem item = menu.findItem(R.id.searchFavourites);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Favourites");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = true;
                if (query.length() == 0) {
                    searchedFavourites.clear();
                    adapter.setFavourites(favouriteViewModel.getAllFavourites().getValue());
                    adapter.notifyDataSetChanged();
                } else
                    searchFavouritesByName(query.toLowerCase().trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                if (newText.length() == 0) {
                    searchedFavourites.clear();
                    adapter.setFavourites(favouriteViewModel.getAllFavourites().getValue());
                    adapter.notifyDataSetChanged();
                } else
                    searchFavouritesByName(newText.toLowerCase().trim());
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                favouriteViewModel.deleteAllFavourites();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private String firstLetterToUppercase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
