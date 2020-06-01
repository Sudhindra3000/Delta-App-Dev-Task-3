package com.example.deltatask3.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
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

import com.example.deltatask3.R;
import com.example.deltatask3.adapters.PokemonAdapter;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.ActivityPokemonsBinding;
import com.example.deltatask3.utils.Pokedex;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.Region;
import com.example.deltatask3.utils.Type;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokemonsActivity extends AppCompatActivity {

    private final String SPEED = "speed", HP = "hp", ATTACK = "attack", DEFENSE = "defense", SP_ATTACK = "special-attack", SP_DEFENSE = "special-defense";
    private static final String TAG = "PokemonsActivity";
    private final int REGIONS = 45, TYPES = 23;
    private int mode, offset = 0;
    private ActivityPokemonsBinding binding;
    private Retrofit retrofit;
    private PokemonApi pokemonApi;
    private LinearLayoutManager layoutManager;
    private PokemonAdapter pokemonAdapter;
    private ArrayList<Pokemon> pokemons, searchedPokemon;
    private androidx.appcompat.widget.SearchView searchView;
    private boolean loading = true, searching = false, submit = false, paginate = true;

    private int regionID;
    private String regionName;
    private Pokedex pokedex;
    private ArrayList<Pokedex> pokedexes;
    private ArrayList<String> names;

    private int typeID;
    private String typeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPokemonsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setEnterTransition(new Explode());

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        setSupportActionBar(binding.toolbarP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pokedex = new Pokedex();
        pokedexes = new ArrayList<>();
        pokemons = new ArrayList<>();
        searchedPokemon = new ArrayList<>();
        names = new ArrayList<>();
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pokeapi.co/api/v2/")
                .build();

        pokemonApi = retrofit.create(PokemonApi.class);

        mode = getIntent().getIntExtra("mode", -1);

        if (mode == REGIONS) {
            regionID = getIntent().getIntExtra("regionID", 0);
            regionID++;
            regionName = getIntent().getStringExtra("regionName");
            getSupportActionBar().setTitle("Pokémon in " + regionName + " Region");
            getRegion();
        } else {
            typeID = getIntent().getIntExtra("typeID", 0);
            typeName = getIntent().getStringExtra("typeName");
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
        pokemonAdapter.setListener(new PokemonAdapter.PokemonAdapterListener() {
            @Override
            public void onItemClicked(int position, ImageView pokemonIv, TextView nameIv) {
                showDetails(position, pokemonIv, nameIv);
            }
        });
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
    }

    private void paginate() {
        if (paginate) {
            offset += 20;
            loadPokemon();
            loading = true;
        }
    }

    private void getRegion() {
        Call<Region> call = pokemonApi.getRegionFromID(regionID);
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
                Call<Pokedex> call = pokemonApi.getPokedexFromName(pokedex1.getName());
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
            Call<Pokedex> call = pokemonApi.getPokedexFromName(pokedex.getName());
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
        Call<Type> call = pokemonApi.getTypeFromID(typeID);
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
            Call<Pokemon> call = pokemonApi.getPokemonFromName(s);
            call.enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        return;
                    }

                    pokemons.set(names.indexOf(s), response.body());
                    pokemonAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    Log.i(TAG, "t=" + t.getLocalizedMessage());
                }
            });
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
        Pokemon pokemon;
        if ((searching | submit) && searchedPokemon.size() != 0)
            pokemon = searchedPokemon.get(position);
        else
            pokemon = pokemons.get(position);
        if (pokemon.getId() != 0 && pokemon.getSprites() != null) {
            Intent intent = new Intent(PokemonsActivity.this, PokemonDetailsActivity.class);
            intent.putExtra("name", pokemon.getName());
            intent.putExtra("id", pokemon.getId());
            intent.putExtra("imageURL", pokemon.getSprites().getFront_default());
            intent.putExtra("types", pokemon.getTypes().size());
            intent.putExtra("type1", pokemon.getTypes().get(0).getType().getName());
            if (pokemon.getTypes().size() == 2)
                intent.putExtra("type2", pokemon.getTypes().get(1).getType().getName());
            intent.putExtra(SPEED, pokemon.getStats().get(0).getBase_stat());
            intent.putExtra(SP_DEFENSE, pokemon.getStats().get(1).getBase_stat());
            intent.putExtra(SP_ATTACK, pokemon.getStats().get(2).getBase_stat());
            intent.putExtra(DEFENSE, pokemon.getStats().get(3).getBase_stat());
            intent.putExtra(ATTACK, pokemon.getStats().get(4).getBase_stat());
            intent.putExtra(HP, pokemon.getStats().get(5).getBase_stat());
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
        searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Pokémon");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "submit");
                searching = false;
                submit = true;
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
                submit = false;
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