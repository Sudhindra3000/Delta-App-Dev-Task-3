package com.example.deltatask3.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.deltatask3.R
import com.example.deltatask3.databinding.ActivityMainBinding
import com.example.deltatask3.fragments.*
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    private var currentTitle = "Pokémon"

    private val fragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setSupportActionBar(binding.toolbar)
        val drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        binding.navView.setNavigationItemSelectedListener(this)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        initFragments()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[0]).commit()
            binding.navView.setCheckedItem(R.id.po)
        }
    }

    private fun initFragments() {
        fragments.add(PokemonsFragment())
        fragments.add(ItemsFragment())
        fragments.add(LocationsFragment())
        fragments.add(TypesFragment())
        fragments.add(RegionsFragment())
        fragments.add(SearchFragment())
        fragments.add(FavouritesFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.po -> if (currentTitle != "Pokémon") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[0]).commit()
                currentTitle = "Pokémon"
            }
            R.id.io -> if (currentTitle != "Items") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[1]).commit()
                currentTitle = "Items"
            }
            R.id.lo -> if (currentTitle != "Locations") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[2]).commit()
                currentTitle = "Locations"
            }
            R.id.to -> if (currentTitle != "Types") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[3]).commit()
                currentTitle = "Types"
            }
            R.id.ro -> if (currentTitle != "Regions") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[4]).commit()
                currentTitle = "Regions"
            }
            R.id.so -> if (currentTitle != "Search") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[5]).commit()
                currentTitle = "Search"
            }
            R.id.fo -> if (currentTitle != "Favourites") {
                supportFragmentManager.beginTransaction().replace(R.id.frContainer, fragments[6]).commit()
                currentTitle = "Favourites"
            }
        }
        supportActionBar!!.title = currentTitle
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }
}