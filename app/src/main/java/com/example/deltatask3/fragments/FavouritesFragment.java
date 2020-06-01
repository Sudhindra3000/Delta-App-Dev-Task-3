package com.example.deltatask3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends Fragment {

    private AppViewModel appViewModel;

    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel=new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Favourites");
    }


}
