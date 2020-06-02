package com.example.deltatask3.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.deltatask3.R;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.ActivityPokemonDetailsBinding;
import com.example.deltatask3.utils.EvolutionChain;
import com.example.deltatask3.utils.PokemonID;
import com.example.deltatask3.utils.PokemonSpecies;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokemonDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PokemonDetailsActivity";
    private final String SPEED = "speed", HP = "hp", ATTACK = "attack", DEFENSE = "defense", SP_ATTACK = "special-attack", SP_DEFENSE = "special-defense";
    private ActivityPokemonDetailsBinding binding;
    private Intent intent;
    private int id;
    private Retrofit retrofit;
    private PokemonApi pokemonApi;
    private String evolutionChainURL;
    private String name, url;
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
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pokeapi.co/api/v2/")
                .build();

        pokemonApi = retrofit.create(PokemonApi.class);

        intent = getIntent();
        id = intent.getIntExtra("id", 0);
        name = intent.getStringExtra("name");
        url = intent.getStringExtra("imageURL");
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
        int types = intent.getIntExtra("types", 1);
        binding.tvType1.setText(firstLetterToUppercase(intent.getStringExtra("type1")));
        if (types == 1) {
            binding.tvTypes.setText("Type");
            binding.tvType2.setVisibility(View.GONE);
        } else
            binding.tvType2.setText(firstLetterToUppercase(intent.getStringExtra("type2")));
    }

    private void getStats() {
        binding.tvSpeed.setText("Speed : " + intent.getIntExtra(SPEED, 0));
        binding.tvHp.setText("Hp : " + intent.getIntExtra(HP, 0));
        binding.tvAttack.setText("Attack : " + intent.getIntExtra(ATTACK, 0));
        binding.tvDefense.setText("Defense : " + intent.getIntExtra(DEFENSE, 0));
        binding.tvSpAttack.setText("Sp. Attack : " + intent.getIntExtra(SP_ATTACK, 0));
        binding.tvSpDefense.setText("Sp. Defense : " + intent.getIntExtra(SP_DEFENSE, 0));
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

                switch (names.size()){
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
                switch (index){
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
