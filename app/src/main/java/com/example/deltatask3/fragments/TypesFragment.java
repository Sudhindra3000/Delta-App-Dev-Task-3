package com.example.deltatask3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deltatask3.activities.PokemonsActivity;
import com.example.deltatask3.databinding.FragmentTypesBinding;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;

import java.util.ArrayList;

public class TypesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "TypesFragment";
    private final int TYPES = 23;
    private AppViewModel appViewModel;
    private FragmentTypesBinding binding;
    private ArrayList<String> types;

    public TypesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTypesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Types");

        initButtons();
        initTypes();
    }

    private void initButtons() {
        binding.button.setOnClickListener(this::onClick);
        binding.button2.setOnClickListener(this::onClick);
        binding.button3.setOnClickListener(this::onClick);
        binding.button4.setOnClickListener(this::onClick);
        binding.button5.setOnClickListener(this::onClick);
        binding.button6.setOnClickListener(this::onClick);
        binding.button7.setOnClickListener(this::onClick);
        binding.button8.setOnClickListener(this::onClick);
        binding.button9.setOnClickListener(this::onClick);
        binding.button10.setOnClickListener(this::onClick);
        binding.button11.setOnClickListener(this::onClick);
        binding.button12.setOnClickListener(this::onClick);
        binding.button13.setOnClickListener(this::onClick);
        binding.button14.setOnClickListener(this::onClick);
        binding.button15.setOnClickListener(this::onClick);
        binding.button16.setOnClickListener(this::onClick);
        binding.button17.setOnClickListener(this::onClick);
        binding.button18.setOnClickListener(this::onClick);
    }

    private void initTypes() {
        types=new ArrayList<>();
        types.add("Normal");
        types.add("Fighting");
        types.add("Flying");
        types.add("Poison");
        types.add("Ground");
        types.add("Rock");
        types.add("Bug");
        types.add("Ghost");
        types.add("Steel");
        types.add("Fire");
        types.add("Water");
        types.add("Grass");
        types.add("Electric");
        types.add("Psychic");
        types.add("Ice");
        types.add("Dragon");
        types.add("Dark");
        types.add("Fairy");
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getActivity(), PokemonsActivity.class);
        intent.putExtra("mode",TYPES);
        intent.putExtra("typeID",Integer.parseInt(v.getTag().toString()));
        intent.putExtra("typeName",types.get(Integer.parseInt(v.getTag().toString())-1));
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
