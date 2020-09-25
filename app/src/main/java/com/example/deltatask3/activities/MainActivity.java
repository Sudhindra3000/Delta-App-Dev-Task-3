package com.example.deltatask3.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.deltatask3.R;
import com.example.deltatask3.databinding.ActivityMainBinding;
import com.example.deltatask3.fragments.FavouritesFragment;
import com.example.deltatask3.fragments.ItemsFragment;
import com.example.deltatask3.fragments.LocationsFragment;
import com.example.deltatask3.fragments.PokemonsFragment;
import com.example.deltatask3.fragments.RegionsFragment;
import com.example.deltatask3.fragments.SearchFragment;
import com.example.deltatask3.fragments.TypesFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private String currentTitle = "Pokémon";

    private ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        initFragments();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(0)).commit();
            binding.navView.setCheckedItem(R.id.po);
        }
    }

    private void initFragments() {
        fragments.add(new PokemonsFragment());
        fragments.add(new ItemsFragment());
        fragments.add(new LocationsFragment());
        fragments.add(new TypesFragment());
        fragments.add(new RegionsFragment());
        fragments.add(new SearchFragment());
        fragments.add(new FavouritesFragment());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.po:
                if (!currentTitle.equals("Pokémon")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(0)).commit();
                    currentTitle = "Pokémon";
                }
                break;
            case R.id.io:
                if (!currentTitle.equals("Items")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(1)).commit();
                    currentTitle = "Items";
                }
                break;
            case R.id.lo:
                if (!currentTitle.equals("Locations")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(2)).commit();
                    currentTitle = "Locations";
                }
                break;
            case R.id.to:
                if (!currentTitle.equals("Types")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(3)).commit();
                    currentTitle = "Types";
                }
                break;
            case R.id.ro:
                if (!currentTitle.equals("Regions")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(4)).commit();
                    currentTitle = "Regions";
                }
                break;
            case R.id.so:
                if (!currentTitle.equals("Search")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(5)).commit();
                    currentTitle = "Search";
                }
                break;
            case R.id.fo:
                if (!currentTitle.equals("Favourites")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, fragments.get(6)).commit();
                    currentTitle = "Favourites";
                }
                break;
        }
        getSupportActionBar().setTitle(currentTitle);
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else {

            super.onBackPressed();
        }
    }
}
