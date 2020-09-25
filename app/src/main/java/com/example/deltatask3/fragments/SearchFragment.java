package com.example.deltatask3.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltatask3.R;
import com.example.deltatask3.activities.PokemonDetailsActivity;
import com.example.deltatask3.adapters.ItemLocationAdapter;
import com.example.deltatask3.adapters.PokemonAdapter;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FragmentSearchBinding;
import com.example.deltatask3.utils.ItemLocation;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.FavouriteViewModel;
import com.google.gson.Gson;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;

    private FavouriteViewModel favouriteViewModel;

    @Inject
    PokemonApi pokemonApi;

    private ArrayList<Pokemon> pokemons = new ArrayList<>();
    private ArrayList<ItemLocation> locations = new ArrayList<>(), items = new ArrayList<>();

    private PokemonAdapter pokemonAdapter;
    private ItemLocationAdapter locationAdapter, itemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favouriteViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavouriteViewModel.class);

        buildRecyclerView();
    }

    private void buildRecyclerView() {
        binding.pokemonCard.setHasFixedSize(true);
        LinearLayoutManager pokemonLayoutManager = new LinearLayoutManager(requireContext());
        pokemonAdapter = new PokemonAdapter();
        pokemonAdapter.setPokemons(pokemons);
        pokemonAdapter.setListener((position, pokemon, name) -> showDetails(pokemon, name));
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (pokemonIsInFavourites(pokemons.get(0))) {
                    pokemonAdapter.notifyItemChanged(0);
                    StyleableToast.makeText(requireContext(), firstLetterToUppercase(pokemons.get(0).getName()) + " is already in favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show();
                } else {
                    StyleableToast.makeText(requireContext(), firstLetterToUppercase(pokemons.get(0).getName()) + " added to favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show();
                    favouriteViewModel.insert(new Favourite(pokemons.get(0)));
                    pokemons.clear();
                    binding.pokemonCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(Color.parseColor("#EB3939"))
                        .addSwipeRightActionIcon(R.drawable.favourite_icon)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.pokemonCard);
        binding.pokemonCard.setLayoutManager(pokemonLayoutManager);
        binding.pokemonCard.setAdapter(pokemonAdapter);


        binding.itemCard.setHasFixedSize(true);
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(requireContext());
        itemAdapter = new ItemLocationAdapter();
        itemAdapter.setItemLocations(items);
        binding.itemCard.setLayoutManager(itemLayoutManager);
        binding.itemCard.setAdapter(itemAdapter);

        binding.locationCard.setHasFixedSize(true);
        LinearLayoutManager locationLayoutManager = new LinearLayoutManager(requireContext());
        locationAdapter = new ItemLocationAdapter();
        locationAdapter.setItemLocations(locations);
        binding.locationCard.setLayoutManager(locationLayoutManager);
        binding.locationCard.setAdapter(locationAdapter);
    }

    private boolean pokemonIsInFavourites(Pokemon pokemon) {
        for (Favourite favourite : favouriteViewModel.getAllFavourites().getValue()) {
            if (favourite.getPokemon().getId() == pokemon.getId())
                return true;
        }
        return false;
    }

    private void showDetails(ImageView pokemonIv, TextView nameIv) {
        if (pokemons.get(0).getId() != 0 && pokemons.get(0).getSprites() != null) {
            Intent intent = new Intent(requireActivity(), PokemonDetailsActivity.class);
            Gson gson = new Gson();
            String pokemonJson = gson.toJson(pokemons.get(0));
            intent.putExtra("pokemonJson", pokemonJson);
            Pair<View, String> imagePair = new Pair<>(pokemonIv, "pokemonImg");
            Pair<View, String> namePair = new Pair<>(nameIv, "pokemonName");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair);
            startActivity(intent, options.toBundle());
        }
    }

    private void search(int id) {
        Call<Pokemon> pokemonCall = pokemonApi.getPokemonFromID(id);
        Call<ItemLocation> itemCall = pokemonApi.getItemFromID(id);
        Call<ItemLocation> locationCall = pokemonApi.getLocationFromID(id);
        pokemonCall.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    binding.pokemonCard.setVisibility(View.GONE);
                    return;
                }

                binding.pokemonCard.setVisibility(View.VISIBLE);
                pokemons.clear();
                pokemons.add(response.body());
                pokemonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
                binding.pokemonCard.setVisibility(View.GONE);
            }
        });
        itemCall.enqueue(new Callback<ItemLocation>() {
            @Override
            public void onResponse(Call<ItemLocation> call, Response<ItemLocation> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    binding.itemCard.setVisibility(View.GONE);
                    return;
                }

                binding.itemCard.setVisibility(View.VISIBLE);
                items.clear();
                items.add(response.body());
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ItemLocation> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
                binding.itemCard.setVisibility(View.GONE);
            }
        });
        locationCall.enqueue(new Callback<ItemLocation>() {
            @Override
            public void onResponse(Call<ItemLocation> call, Response<ItemLocation> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    binding.locationCard.setVisibility(View.GONE);
                    return;
                }

                binding.locationCard.setVisibility(View.VISIBLE);
                locations.clear();
                locations.add(response.body());
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ItemLocation> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
                binding.locationCard.setVisibility(View.GONE);
            }
        });
    }

    private String firstLetterToUppercase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Enter ID");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setOnCloseListener(() -> {
            binding.tvS.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty())
                    search(Integer.parseInt(query.trim()));
                else
                    search(0);
                binding.tvS.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty())
                    search(Integer.parseInt(newText.trim()));
                else
                    search(0);
                binding.tvS.setVisibility(View.INVISIBLE);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}