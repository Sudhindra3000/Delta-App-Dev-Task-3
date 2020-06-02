package com.example.deltatask3.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;

import com.example.deltatask3.fragments.SearchFragment;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;
import com.example.deltatask3.databinding.ActivityMainBinding;
import com.example.deltatask3.fragments.FavouritesFragment;
import com.example.deltatask3.fragments.ItemsFragment;
import com.example.deltatask3.fragments.LocationsFragment;
import com.example.deltatask3.fragments.PokemonsFragment;
import com.example.deltatask3.fragments.RegionsFragment;
import com.example.deltatask3.fragments.TypesFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private AppViewModel appViewModel;
    private ActionBarDrawerToggle drawerToggle;
    private String currentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        setSupportActionBar(binding.toolbar);

        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        appViewModel.getCurrentTitle().observe(this, s -> {
            currentTitle = s;
            getSupportActionBar().setTitle(s);
        });

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new PokemonsFragment()).commit();
            binding.navView.setCheckedItem(R.id.po);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else {

            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.po:
                if (!currentTitle.equals("Pokémon"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new PokemonsFragment()).commit();
                break;
            case R.id.io:
                if (!currentTitle.equals("Items"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new ItemsFragment()).commit();
                break;
            case R.id.lo:
                if (!currentTitle.equals("Locations"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new LocationsFragment()).commit();
                break;
            case R.id.to:
                if (!currentTitle.equals("Types"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new TypesFragment()).commit();
                break;
            case R.id.ro:
                if (!currentTitle.equals("Regions"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new RegionsFragment()).commit();
                break;
            case R.id.so:
                if (!currentTitle.equals("Search"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new SearchFragment()).commit();
                break;
            case R.id.fo:
                if (!currentTitle.equals("Favourites"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.frContainer, new FavouritesFragment()).commit();
                break;
        }
        onBackPressed();
        return true;
    }
}
