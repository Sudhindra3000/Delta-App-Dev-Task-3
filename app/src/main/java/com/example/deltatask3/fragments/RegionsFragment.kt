package com.example.deltatask3.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.deltatask3.activities.PokemonsActivity;
import com.example.deltatask3.databinding.FragmentRegionsBinding;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegionsFragment extends Fragment implements View.OnClickListener {

    private FragmentRegionsBinding binding;
    private ArrayList<String> regions;

    public RegionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initButtons();
        initRegions();
    }

    private void initButtons() {
        binding.btA.setOnClickListener(this::onClick);
        binding.btH.setOnClickListener(this::onClick);
        binding.btJ.setOnClickListener(this::onClick);
        binding.btKl.setOnClickListener(this::onClick);
        binding.btKt.setOnClickListener(this::onClick);
        binding.btS.setOnClickListener(this::onClick);
        binding.btU.setOnClickListener(this::onClick);
    }

    private void initRegions() {
        regions = new ArrayList<>();
        regions.add("Kanto");
        regions.add("Johto");
        regions.add("Hoenn");
        regions.add("Sinnoh");
        regions.add("Unova");
        regions.add("Kalos");
        regions.add("Alola");
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), PokemonsActivity.class);
        int REGIONS = 45;
        intent.putExtra("mode", REGIONS);
        intent.putExtra("regionID", Integer.parseInt(v.getTag().toString()));
        intent.putExtra("regionName", regions.get(Integer.parseInt(v.getTag().toString())));
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
