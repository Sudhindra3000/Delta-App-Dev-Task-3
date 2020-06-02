package com.example.deltatask3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deltatask3.adapters.FavouriteAdapter;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FragmentFavouritesBinding;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;
import com.example.deltatask3.viewmodels.FavouriteViewModel;

import java.util.List;


public class FavouritesFragment extends Fragment {

    private static final String TAG = "FavouritesFragment";
    private AppViewModel appViewModel;
    private FragmentFavouritesBinding binding;
    private FavouriteViewModel favouriteViewModel;
    private FavouriteAdapter adapter;
    private LinearLayoutManager layoutManager;


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
                }
                adapter.setFavourites(favourites);
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
                String name = adapter.getFavouriteAt(pos).getPokemon().getName();
                Log.i(TAG, "onItemClicked: Name=" + name);
            }
        });

        binding.favourites.setLayoutManager(layoutManager);
        binding.favourites.setAdapter(adapter);
    }
}
