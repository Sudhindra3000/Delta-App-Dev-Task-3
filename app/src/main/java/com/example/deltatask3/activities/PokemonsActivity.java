package com.example.deltatask3.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltatask3.R;
import com.example.deltatask3.adapters.PokemonAdapter;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.ActivityPokemonsBinding;
import com.example.deltatask3.utils.Pokedex;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.Region;
import com.example.deltatask3.utils.Type;
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

import static com.example.deltatask3.UtilsKt.firstLetterToUppercase;

@AndroidEntryPoint
public class PokemonsActivity extends AppCompatActivity {

    private static final String TAG = "PokemonsActivity";
    private ActivityPokemonsBinding binding;

    private int offset = 0;

    private FavouriteViewModel favouriteViewModel;
    private ArrayList<Favourite> favourites = new ArrayList<>();

    @Inject
    PokemonApi pokemonApi;

    private LinearLayoutManager layoutManager;
    private PokemonAdapter pokemonAdapter;
    private ArrayList<Pokemon> pokemons = new ArrayList<>(), searchedPokemon = new ArrayList<>();
    private boolean loading = true, searching = false, paginate = true;

    private int regionID;
    private Pokedex pokedex;
    private ArrayList<Pokedex> pokedexes = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

    private int typeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPokemonsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setEnterTransition(new Explode());
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        setSupportActionBar(binding.toolbarP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        favouriteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(FavouriteViewModel.class);
        favouriteViewModel.getAllFavourites().observe(this, newFavourites -> {
            favourites.clear();
            favourites.addAll(newFavourites);
        });

        int mode = getIntent().getIntExtra("mode", -1);

        int REGIONS = 45;
        if (mode == REGIONS) {
            regionID = getIntent().getIntExtra("regionID", 0);
            regionID++;
            String regionName = getIntent().getStringExtra("regionName");
            getSupportActionBar().setTitle("Pokémon in " + regionName + " Region");
            getRegion();
        } else {
            typeID = getIntent().getIntExtra("typeID", 0);
            String typeName = getIntent().getStringExtra("typeName");
            getSupportActionBar().setTitle("Pokémon having " + typeName + " Type");
            getType();
        }

        buildRecyclerView();
    }

    private void buildRecyclerView() {
        binding.pokemonsList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        pokemonAdapter = new PokemonAdapter();
        pokemonAdapter.setPokemons(pokemons);
        pokemonAdapter.setListener(this::showDetails);
        binding.pokemonsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            Log.i(TAG, "onScrolled: LastItem");
                            paginate();
                        }
                    }
                }
            }
        });
        binding.pokemonsList.setLayoutManager(layoutManager);
        binding.pokemonsList.setAdapter(pokemonAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Pokemon swipedPokemon = pokemonAdapter.getPokemonAt(viewHolder.getAdapterPosition());
                if (pokemonIsInFavourites(swipedPokemon)) {
                    pokemonAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    StyleableToast.makeText(getApplicationContext(), firstLetterToUppercase(swipedPokemon.getName()) + " is already in favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show();
                } else {
                    addToFavourites(viewHolder.getAdapterPosition(), swipedPokemon);
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
        }).attachToRecyclerView(binding.pokemonsList);
    }

    private boolean pokemonIsInFavourites(Pokemon pokemon) {
        for (Favourite favourite : favourites) {
            if (favourite.getPokemon().getId() == pokemon.getId())
                return true;
        }
        return false;
    }

    private void addToFavourites(int position, Pokemon pokemon) {
        StyleableToast.makeText(this, firstLetterToUppercase(pokemon.getName()) + " added to favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show();
        favouriteViewModel.insert(new Favourite(pokemon));
        offset--;
        names.remove(pokemon.getName());
        pokemons.remove(pokemon);
        if (searching)
            searchedPokemon.remove(pokemon);
        pokemonAdapter.notifyItemRemoved(position);
    }

    private void paginate() {
        if (paginate) {
            offset += 20;
            loadPokemon();
            loading = true;
        }
    }

    private void getRegion() {
        Call<Region> call = pokemonApi.getRegion(regionID);
        call.enqueue(new Callback<Region>() {
            @Override
            public void onResponse(Call<Region> call, Response<Region> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }

                switch (regionID) {
                    case 1:
                        pokedex = response.body().getPokedexes().get(0);
                        break;
                    case 6:
                        pokedexes.addAll(response.body().getPokedexes());
                        break;
                    default:
                        pokedex = response.body().getPokedexes().get(1);
                        break;
                }

                Log.i(TAG, "size=" + names.size());
                loadPokedexForRegion();
            }

            @Override
            public void onFailure(Call<Region> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
            }
        });
    }

    private void loadPokedexForRegion() {
        if (regionID == 6) {
            ArrayList<String> names2 = new ArrayList<>(), names3 = new ArrayList<>();
            for (Pokedex pokedex1 : pokedexes) {
                Call<Pokedex> call = pokemonApi.getPokedex(pokedex1.getName());
                call.enqueue(new Callback<Pokedex>() {
                    @Override
                    public void onResponse(Call<Pokedex> call, Response<Pokedex> response) {
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "onResponse: " + response);
                            return;
                        }


                        for (Pokedex.PokemonEntry pokemonEntry : response.body().getPokemon_entries()) {
                            String name = pokemonEntry.getPokemon_species().getName();
                            switch (pokedexes.indexOf(pokedex1)) {
                                case 0:
                                    names.add(name);
                                    break;
                                case 1:
                                    names2.add(name);
                                    break;
                                case 2:
                                    names3.add(name);
                                    break;
                            }
                        }

                        if (pokedexes.indexOf(pokedex1) == 2) {
                            names.addAll(names2);
                            names.addAll(names3);
                            names2.clear();
                            names3.clear();
                            Log.i(TAG, "size=" + names.size());
                            loadPokemon();
                        }
                    }

                    @Override
                    public void onFailure(Call<Pokedex> call, Throwable t) {
                        Log.i(TAG, "t=" + t.getLocalizedMessage());
                    }
                });
            }

        } else {
            Call<Pokedex> call = pokemonApi.getPokedex(pokedex.getName());
            call.enqueue(new Callback<Pokedex>() {
                @Override
                public void onResponse(Call<Pokedex> call, Response<Pokedex> response) {
                    if (!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        return;
                    }

                    for (Pokedex.PokemonEntry pokemonEntry : response.body().getPokemon_entries()) {
                        names.add(pokemonEntry.getPokemon_species().getName());
                    }
                    Log.i(TAG, "size=" + names.size());

                    loadPokemon();
                }

                @Override
                public void onFailure(Call<Pokedex> call, Throwable t) {
                    Log.i(TAG, "t=" + t.getLocalizedMessage());
                }
            });
        }
    }

    private void getType() {
        Call<Type> call = pokemonApi.getType(typeID);
        call.enqueue(new Callback<Type>() {
            @Override
            public void onResponse(Call<Type> call, Response<Type> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }

                for (Type.TypePokemon typePokemon : response.body().getPokemon()) {
                    names.add(typePokemon.getPokemon().getName());
                }

                Log.i(TAG, "size=" + names.size());
                loadPokemon();
            }

            @Override
            public void onFailure(Call<Type> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
            }
        });
    }

    private void loadPokemon() {
        int toIndex;
        if (offset + 20 > names.size() - 1) {
            toIndex = names.size() - 1;
            paginate = false;
        } else
            toIndex = offset + 20;
        Log.i(TAG, "from:" + offset + ", to:" + toIndex);
        for (String s : names.subList(offset, toIndex)) {
            pokemons.add(new Pokemon(s));
            //Todo: Complete this using Coroutines

//            Call<Pokemon> call = pokemonApi.getPokemon(s);
//            call.enqueue(new Callback<Pokemon>() {
//                @Override
//                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
//                    if (!response.isSuccessful()) {
//                        Log.i(TAG, "onResponse: " + response);
//                        return;
//                    }
//
//                    if (names.indexOf(s) >= 0)
//                        pokemons.set(names.indexOf(s), response.body());
//                    if (searching)
//                        searchPokemonByName(searchView.getQuery().toString().trim().toLowerCase());
//                    pokemonAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onFailure(Call<Pokemon> call, Throwable t) {
//                    Log.i(TAG, "t=" + t.getLocalizedMessage());
//                }
//            });
        }
    }

    private void searchPokemonByName(String name) {
        name = name.trim().toLowerCase();
        searchedPokemon.clear();

        for (Pokemon pokemon : pokemons) {
            if (pokemon.getName().trim().contains(name))
                searchedPokemon.add(pokemon);
        }

        pokemonAdapter.setPokemons(searchedPokemon);
        pokemonAdapter.notifyDataSetChanged();
    }

    private void showDetails(int position, ImageView pokemonIv, TextView nameIv) {
        Pokemon pokemon = pokemonAdapter.getPokemonAt(position);
        if (pokemon.getId() != 0 && pokemon.getSprites() != null) {
            Intent intent = new Intent(PokemonsActivity.this, PokemonDetailsActivity.class);
            Gson gson = new Gson();
            String pokemonJson = gson.toJson(pokemon);
            intent.putExtra("pokemonJson", pokemonJson);
            Pair<View, String> imagePair = new Pair<>(pokemonIv, "pokemonImg");
            Pair<View, String> namePair = new Pair<>(nameIv, "pokemonName");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(PokemonsActivity.this, imagePair, namePair);
            startActivity(intent, options.toBundle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Pokémon");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "submit");
                searching = true;
                if (query.length() == 0) {
                    searchedPokemon.clear();
                    pokemonAdapter.setPokemons(pokemons);
                    pokemonAdapter.notifyDataSetChanged();
                } else
                    searchPokemonByName(query.toLowerCase().trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                if (newText.length() == 0) {
                    searchedPokemon.clear();
                    pokemonAdapter.setPokemons(pokemons);
                    pokemonAdapter.notifyDataSetChanged();
                } else
                    searchPokemonByName(newText.toLowerCase().trim());
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}