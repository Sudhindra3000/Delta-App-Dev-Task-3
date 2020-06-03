package com.example.deltatask3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.example.deltatask3.adapters.FavouriteAdapter;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FragmentFavouritesBinding;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;
import com.example.deltatask3.viewmodels.FavouriteViewModel;
import com.google.gson.Gson;

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
                    setHasOptionsMenu(false);
                } else
                    setHasOptionsMenu(true);
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
            public void onItemClicked(int pos) {
                showDetails(adapter.getFavouriteAt(pos).getPokemon());
            }
        });

        binding.favourites.setLayoutManager(layoutManager);
        binding.favourites.setAdapter(adapter);
    }

    private void showDetails(Pokemon pokemon){
        Gson gson=new Gson();
        String pokemonJson=gson.toJson(pokemon);
    }

    private void searchFavouritesByName(String name) {
        searchedFavourites.clear();

        for (Favourite favourite :favouriteViewModel.getAllFavourites().getValue()) {
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
}
