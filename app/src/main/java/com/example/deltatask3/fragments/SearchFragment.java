package com.example.deltatask3.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

import com.example.deltatask3.R;
import com.example.deltatask3.activities.PokemonDetailsActivity;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.FragmentSearchBinding;
import com.example.deltatask3.utils.ItemLocation;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchFragment extends Fragment {

    private final String SPEED = "speed", HP = "hp", ATTACK = "attack", DEFENSE = "defense", SP_ATTACK = "special-attack", SP_DEFENSE = "special-defense";
    private static final String TAG = "SearchFragment";
    private AppViewModel appViewModel;
    private FragmentSearchBinding binding;
    private Retrofit retrofit;
    private PokemonApi pokemonApi;
    private Pokemon pokemon;
    private ItemLocation item, location;
    private SearchView searchView;

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
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Search");

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pokeapi.co/api/v2/")
                .build();
        pokemonApi = retrofit.create(PokemonApi.class);

        binding.pokemonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails(binding.ivSPImg, binding.tvSPName);
            }
        });
    }

    private void showDetails(ImageView pokemonIv, TextView nameIv) {
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
                pokemon = response.body();
                binding.tvSPId.setText(String.valueOf(pokemon.getId()));
                binding.tvSPName.setText(firstLetterToUppercase(pokemon.getName()));
                Picasso.get().load(pokemon.getSprites().getFront_default()).placeholder(R.drawable.placeholder_image).into(binding.ivSPImg);
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
                item = response.body();
                binding.tvSIId.setText(String.valueOf(item.getId()));
                binding.tvSIName.setText(firstLetterToUppercase(item.getName()));
                Picasso.get().load(item.getSprite().getDefault_sprite()).placeholder(R.drawable.placeholder_image).into(binding.ivSIImg);
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
                location = response.body();
                binding.tvSLId.setText(String.valueOf(location.getId()));
                binding.tvSLName.setText(firstLetterToUppercase(location.getName()));
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
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Enter ID");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                binding.tvS.setVisibility(View.VISIBLE);
                return false;
            }
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