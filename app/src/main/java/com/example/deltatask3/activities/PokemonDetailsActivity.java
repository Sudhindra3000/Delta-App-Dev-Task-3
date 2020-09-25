package com.example.deltatask3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deltatask3.R;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.ActivityPokemonDetailsBinding;
import com.example.deltatask3.utils.EvolutionChain;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.PokemonID;
import com.example.deltatask3.utils.PokemonSpecies;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PokemonDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PokemonDetailsActivity";
    private ActivityPokemonDetailsBinding binding;

    @Inject
    PokemonApi pokemonApi;

    private int id;
    private String evolutionChainURL;
    private Pokemon pokemon;

    private ArrayList<String> names;
    private ArrayList<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPokemonDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        getWindow().getSharedElementEnterTransition().setDuration(500);
        getWindow().setEnterTransition(new Explode());
        getWindow().getEnterTransition().setDuration(500);
        names = new ArrayList<>();
        urls = new ArrayList<>();

        Intent intent = getIntent();
        String pokemonJson = intent.getStringExtra("pokemonJson");
        pokemon = new Gson().fromJson(pokemonJson, Pokemon.class);
        id = pokemon.getId();
        String name = pokemon.getName();
        String url = pokemon.getSprites().getFront_default();
        getTypes();
        getStats();
        getSpecies();

        binding.tvDetailsId.setText("ID:" + id);
        binding.tvDetailsName.setText(firstLetterToUppercase(name));
        Picasso.get()
                .load(url)
                .into(binding.ivDetailsPokemon);
    }

    private void getTypes() {
        binding.tvType1.setText(firstLetterToUppercase(pokemon.getTypes().get(0).getType().getName()));
        if (pokemon.getTypes().size() == 1) {
            binding.tvTypes.setText("Type");
            binding.tvType2.setVisibility(View.GONE);
        } else
            binding.tvType2.setText(firstLetterToUppercase(pokemon.getTypes().get(1).getType().getName()));
    }

    private void getStats() {
        binding.tvSpeed.setText("Speed : " + pokemon.getStats().get(0).getBase_stat());
        binding.tvHp.setText("Hp : " + pokemon.getStats().get(5).getBase_stat());
        binding.tvAttack.setText("Attack : " + pokemon.getStats().get(4).getBase_stat());
        binding.tvDefense.setText("Defense : " + pokemon.getStats().get(3).getBase_stat());
        binding.tvSpAttack.setText("Sp. Attack : " + pokemon.getStats().get(2).getBase_stat());
        binding.tvSpDefense.setText("Sp. Defense : " + pokemon.getStats().get(1).getBase_stat());
    }

    private void getSpecies() {
        Call<PokemonSpecies> call = pokemonApi.getSpeciesFromID(id);
        call.enqueue(new Callback<PokemonSpecies>() {
            @Override
            public void onResponse(Call<PokemonSpecies> call, Response<PokemonSpecies> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }

                evolutionChainURL = response.body().getEvolution_chain().getUrl();
                getEvolutionChain();
            }

            @Override
            public void onFailure(Call<PokemonSpecies> call, Throwable t) {
                Log.i(TAG, "getSpecies failed");
            }
        });
    }

    private void getEvolutionChain() {
        Call<EvolutionChain> call = pokemonApi.getEvolutionChainFromID(evolutionChainURL.substring(42));

        call.enqueue(new Callback<EvolutionChain>() {
            @Override
            public void onResponse(Call<EvolutionChain> call, Response<EvolutionChain> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }

                EvolutionChain evolutionChain = response.body();
                EvolutionChain.ChainLink chainLink = evolutionChain.getChain();
                String name1 = chainLink.getSpecies().getName();
                names.add(name1);
                urls.add("");
                if (chainLink.getEvolves_to().size() != 0) {
                    String name2 = chainLink.getEvolves_to().get(0).getSpecies().getName();
                    names.add(name2);
                    urls.add("");
                    if (chainLink.getEvolves_to().get(0).getEvolves_to().size() != 0) {
                        String name3 = chainLink.getEvolves_to().get(0).getEvolves_to().get(0).getSpecies().getName();
                        names.add(name3);
                        urls.add("");
                    }
                }

                switch (names.size()) {
                    case 3:
                        binding.evolutionCard3.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        binding.evolutionCard2.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        binding.evolutionCard1.setVisibility(View.VISIBLE);
                        break;
                }
                loadEvolutionChain();
            }

            @Override
            public void onFailure(Call<EvolutionChain> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
            }
        });
    }

    private void loadEvolutionChain() {
        for (String string : names) {
            Call<PokemonID> call = pokemonApi.getPokemonIDFromName(string);
            call.enqueue(new Callback<PokemonID>() {
                @Override
                public void onResponse(Call<PokemonID> call, Response<PokemonID> response) {
                    if (!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        return;
                    }

                    urls.set(names.indexOf(string), getSpriteURLFromID(response.body().getId()));
                    loadSprites(names.indexOf(string));
                }

                @Override
                public void onFailure(Call<PokemonID> call, Throwable t) {
                    Log.i(TAG, "t=" + t.getLocalizedMessage());
                }
            });
        }
    }

    private void loadSprites(int index) {
        switch (names.size()) {
            case 3:
                switch (index) {
                    case 0:
                        Picasso.get().load(urls.get(0)).placeholder(R.drawable.placeholder_image).into(binding.iv31);
                        binding.tv31.setText(firstLetterToUppercase(names.get(0)));
                        break;
                    case 1:
                        Picasso.get().load(urls.get(1)).placeholder(R.drawable.placeholder_image).into(binding.iv32);
                        binding.tv32.setText(firstLetterToUppercase(names.get(1)));
                        break;
                    case 2:
                        Picasso.get().load(urls.get(2)).placeholder(R.drawable.placeholder_image).into(binding.iv33);
                        binding.tv33.setText(firstLetterToUppercase(names.get(2)));
                        break;
                }
                break;
            case 2:
                switch (index) {
                    case 0:
                        Picasso.get().load(urls.get(0)).placeholder(R.drawable.placeholder_image).into(binding.iv21);
                        binding.tv21.setText(firstLetterToUppercase(names.get(0)));
                        break;
                    case 1:
                        Picasso.get().load(urls.get(1)).placeholder(R.drawable.placeholder_image).into(binding.iv22);
                        binding.tv22.setText(firstLetterToUppercase(names.get(1)));
                        break;
                }
                break;
            case 1:
                Picasso.get().load(urls.get(0)).placeholder(R.drawable.placeholder_image).into(binding.iv11);
                binding.tv11.setText(firstLetterToUppercase(names.get(0)));
                break;
        }
    }

    private String getSpriteURLFromID(int id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
    }

    private String firstLetterToUppercase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
