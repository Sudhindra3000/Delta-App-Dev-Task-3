package com.example.deltatask3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.example.deltatask3.adapters.ItemLocationAdapter;
import com.example.deltatask3.api.PokemonApi;
import com.example.deltatask3.databinding.FragmentLocationsBinding;
import com.example.deltatask3.utils.ItemLocation;
import com.example.deltatask3.utils.SearchResult;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationsFragment extends Fragment {

    private static final String TAG = "LocationsFragment";
    private AppViewModel appViewModel;
    private FragmentLocationsBinding binding;
    private Retrofit retrofit;
    private PokemonApi pokemonApi;
    private ArrayList<String> names;
    private ArrayList<ItemLocation> locations, searchedLocations;
    private ItemLocationAdapter adapter;
    private LinearLayoutManager layoutManager;
    private int offset = 0;
    private SearchView searchView;
    private boolean loading = true, searching = false, submit = false;

    public LocationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentLocationsBinding.inflate(inflater,container,false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel=new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Locations");

        names=new ArrayList<>();
        locations=new ArrayList<>();
        searchedLocations=new ArrayList<>();
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pokeapi.co/api/v2/")
                .build();
        pokemonApi = retrofit.create(PokemonApi.class);

        buildRecyclerView();
        getLocations();
    }

    private void buildRecyclerView() {
        binding.allLocations.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(requireContext());
        adapter = new ItemLocationAdapter();
        adapter.setItemLocations(locations);
        binding.allLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        binding.allLocations.setLayoutManager(layoutManager);
        binding.allLocations.setAdapter(adapter);
    }

    private void paginate(){
        offset+=20;
        getLocations();
    }

    private void getLocations(){
        Call<SearchResult> call=pokemonApi.getLocationsWithOffsetAndLimit(offset,20);
        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "onResponse: " + response);
                    return;
                }

                for (SearchResult.Result result : response.body().getResults()) {
                    names.add(result.getName());
                    locations.add(new ItemLocation(result.getName()));
                }

                loading=true;
                loadLocations(names);
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Log.i(TAG, "t=" + t.getLocalizedMessage());
            }
        });
    }

    private void loadLocations(ArrayList<String> names){
        for (String s :names) {
            Call<ItemLocation> call=pokemonApi.getLocationFromName(s);
            call.enqueue(new Callback<ItemLocation>() {
                @Override
                public void onResponse(Call<ItemLocation> call, Response<ItemLocation> response) {
                    if (!response.isSuccessful()) {
                        Log.i(TAG, "onResponse: " + response);
                        return;
                    }

                    locations.set(names.indexOf(s),response.body());

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ItemLocation> call, Throwable t) {
                    Log.i(TAG, "t=" + t.getLocalizedMessage());
                }
            });
        }
    }

    private void searchLocationsByName(String name){
        name = name.trim().toLowerCase();
        searchedLocations.clear();

        for (ItemLocation item : locations) {
            if (item.getName().trim().contains(name))
                searchedLocations.add(item);
        }

        adapter.setItemLocations(searchedLocations);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Locations");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                submit = false;
                if (newText.length() == 0) {
                    searchedLocations.clear();
                    adapter.setItemLocations(locations);
                    adapter.notifyDataSetChanged();
                } else
                    searchLocationsByName(newText.toLowerCase().trim());
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
