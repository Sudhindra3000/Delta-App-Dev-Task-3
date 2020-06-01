package com.example.deltatask3.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.deltatask3.R;
import com.example.deltatask3.activities.PokemonDetailsActivity;
import com.example.deltatask3.adapters.PokemonAdapter;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.FragmentPokemonsBinding;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.SearchResult;
import com.example.deltatask3.viewmodels.AppViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PokemonsFragment extends Fragment {

    private final String SPEED = "speed", HP = "hp", ATTACK = "attack", DEFENSE = "defense", SP_ATTACK = "special-attack", SP_DEFENSE = "special-defense";
    private FragmentPokemonsBinding binding;
    private static final String TAG = "PokemonsFragment";
    private AppViewModel appViewModel;
    private Retrofit retrofit;
    private PokemonApi pokemonApi;
    private ArrayList<String> names;
    private ArrayList<Pokemon> allPokemons;
    private ArrayList<Pokemon> searchedPokemon;
    private PokemonAdapter pokemonAdapter;
    private LinearLayoutManager layoutManager;
    private int offset = 0;
    private SearchView searchView;
    private boolean loading = true, searching = false, submit = false;

    public PokemonsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPokemonsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Pokémon");

        names = new ArrayList<>();
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pokeapi.co/api/v2/")
                .build();

        pokemonApi = retrofit.create(PokemonApi.class);

        allPokemons = new ArrayList<>();
        searchedPokemon = new ArrayList<>();

        buildRecyclerView();
        getMorePokemon();
    }

    private void buildRecyclerView() {
        binding.allPokemonList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(requireContext());
        pokemonAdapter = new PokemonAdapter();
        pokemonAdapter.setPokemons(allPokemons);
        pokemonAdapter.setListener(new PokemonAdapter.PokemonAdapterListener() {
            @Override
            public void onItemClicked(int position, ImageView pokemonIv, TextView nameIv) {
                showDetails(position, pokemonIv, nameIv);
            }
        });
        binding.allPokemonList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        binding.allPokemonList.setLayoutManager(layoutManager);
        binding.allPokemonList.setAdapter(pokemonAdapter);
    }

    private void paginate() {
        offset += 20;
        getMorePokemon();
    }

    private void getMorePokemon() {
        Call<SearchResult> pokemonSearchResultCall = pokemonApi.getPokemonWithOffsetAndLimit(offset, 20);
        pokemonSearchResultCall.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }


                for (SearchResult.Result result : response.body().getResults()) {
                    names.add(result.getName());
                    allPokemons.add(new Pokemon(result.getName()));
                }

                loading = true;
                loadDetailsIntoPokemons(names);
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Log.i(TAG, "showAllPokemon failed");
            }
        });
    }

    private void loadDetailsIntoPokemons(ArrayList<String> names) {
        for (String s : names) {
            Call<Pokemon> call = pokemonApi.getPokemonFromName(s);
            call.enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        return;
                    }

                    allPokemons.set(names.indexOf(s), response.body());
                    if (searching)
                        searchPokemonByName(searchView.getQuery().toString().trim().toLowerCase());
                    pokemonAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    Log.i(TAG, "showAllPokemon failed");
                }
            });
        }
    }

    private void searchPokemonByName(String name) {
        name = name.trim().toLowerCase();
        searchedPokemon.clear();

        for (Pokemon pokemon : allPokemons) {
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
            pokemon = allPokemons.get(position);
        if (pokemon.getId() != 0 && pokemon.getSprites() != null) {
            Intent intent = new Intent(requireActivity(), PokemonDetailsActivity.class);
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
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair);
            startActivity(intent, options.toBundle());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
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
                    pokemonAdapter.setPokemons(allPokemons);
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
                    pokemonAdapter.setPokemons(allPokemons);
                    pokemonAdapter.notifyDataSetChanged();
                } else
                    searchPokemonByName(newText.toLowerCase().trim());
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding=null;
    }
}
