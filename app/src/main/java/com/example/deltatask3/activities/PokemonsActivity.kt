package com.example.deltatask3.activities

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.PokemonAdapter
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.databinding.ActivityPokemonsBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.Pokedex
import com.example.deltatask3.models.Pokemon
import com.example.deltatask3.showSnackbarInMain
import com.example.deltatask3.viewmodels.FavouriteViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PokemonsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonsBinding

    private var offset = 0
    private var favouriteViewModel: FavouriteViewModel? = null
    private val favourites = ArrayList<Favourite>()

    private lateinit var searchView: SearchView

    @Inject
    lateinit var pokemonApi: PokemonApi

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var pokemonAdapter: PokemonAdapter
    private val pokemons = ArrayList<Pokemon>()
    private val searchedPokemon = ArrayList<Pokemon>()

    private var loading = true
    private var searching = false
    private var paginate = true
    private var regionID = 0

    private var pokedex: Pokedex? = null
    private val pokedexes = ArrayList<Pokedex>()
    private val names = ArrayList<String>()
    private var typeID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.enterTransition = Explode()
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setSupportActionBar(binding.toolbarP)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        favouriteViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(FavouriteViewModel::class.java)
        favouriteViewModel!!.allFavourites.observe(this, { newFavourites: List<Favourite>? ->
            favourites.clear()
            favourites.addAll(newFavourites!!)
        })
        val mode = intent.getIntExtra("mode", -1)
        val REGIONS = 45
        if (mode == REGIONS) {
            regionID = intent.getIntExtra("regionID", 0)
            regionID++
            val regionName = intent.getStringExtra("regionName")
            supportActionBar!!.title = "Pokémon in $regionName Region"
            getRegion()
        } else {
            typeID = intent.getIntExtra("typeID", 0)
            val typeName = intent.getStringExtra("typeName")
            supportActionBar!!.title = "Pokémon having $typeName Type"
            getType()
        }
        buildRecyclerView()
    }

    private fun buildRecyclerView() {
        binding.pokemonsList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        pokemonAdapter = PokemonAdapter()
        pokemonAdapter.setPokemons(pokemons)
        pokemonAdapter.setListener { position: Int, pokemonIv: ImageView, nameIv: TextView -> showDetails(position, pokemonIv, nameIv) }
        binding.pokemonsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                    if (loading) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loading = false
                            Log.i(TAG, "onScrolled: LastItem")
                            paginate()
                        }
                    }
                }
            }
        })
        binding.pokemonsList.layoutManager = layoutManager
        binding.pokemonsList.adapter = pokemonAdapter
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipedPokemon = pokemonAdapter.getPokemonAt(viewHolder.adapterPosition)
                if (pokemonIsInFavourites(swipedPokemon)) {
                    pokemonAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    StyleableToast.makeText(applicationContext, firstLetterToUppercase(swipedPokemon.name) + " is already in favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
                } else {
                    addToFavourites(viewHolder.adapterPosition, swipedPokemon)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(Color.parseColor("#EB3939"))
                        .addSwipeRightActionIcon(R.drawable.favourite_icon)
                        .create()
                        .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(binding.pokemonsList)
    }

    private fun pokemonIsInFavourites(pokemon: Pokemon): Boolean {
        for (favourite in favourites) {
            if (favourite.pokemon.id == pokemon.id) return true
        }
        return false
    }

    private fun addToFavourites(position: Int, pokemon: Pokemon) {
        StyleableToast.makeText(this, firstLetterToUppercase(pokemon.name) + " added to favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
        favouriteViewModel!!.insert(Favourite(pokemon))
        offset--
        names.remove(pokemon.name)
        pokemons.remove(pokemon)
        if (searching) searchedPokemon.remove(pokemon)
        pokemonAdapter.notifyItemRemoved(position)
    }

    private fun paginate() {
        if (paginate) {
            offset += 20
            loadPokemon()
            loading = true
        }
    }

    private fun getRegion() {
        lifecycleScope.launch(Dispatchers.IO) {
            val regionResponse = pokemonApi.getRegion(regionID)
            if (!regionResponse.isSuccessful)
                showSnackbarInMain(binding.root, "Failed to fetch Region", Snackbar.LENGTH_SHORT)
            else {
                when (regionID) {
                    1 -> pokedex = regionResponse.body()!!.pokedexes[0]
                    6 -> pokedexes.addAll(regionResponse.body()!!.pokedexes)
                    else -> pokedex = regionResponse.body()!!.pokedexes[1]
                }
                loadPokedexForRegion()
            }
        }
    }

    private suspend fun loadPokedexForRegion() {
        if (regionID == 6) {
            val names2 = ArrayList<String>()
            val names3 = ArrayList<String>()
            for (pokedex1 in pokedexes) {
                val pokedexResponse = pokemonApi.getPokedex(pokedex1.name)
                if (!pokedexResponse.isSuccessful)
                    showSnackbarInMain(binding.root, "Failed to fetch Pokemon", Snackbar.LENGTH_SHORT)
                else {
                    for (pokemonEntry in pokedexResponse.body()!!.pokemon_entries) {
                        val name = pokemonEntry.pokemon_species.name
                        when (pokedexes.indexOf(pokedex1)) {
                            0 -> names.add(name)
                            1 -> names2.add(name)
                            2 -> names3.add(name)
                        }
                    }
                    if (pokedexes.indexOf(pokedex1) == 2) {
                        names.addAll(names2)
                        names.addAll(names3)
                        loadPokemon()
                    }
                }
            }
        } else {
            val pokedexResponse = pokemonApi.getPokedex(pokedex!!.name)
            if (!pokedexResponse.isSuccessful)
                showSnackbarInMain(binding.root, "Failed to fetch Pokemon", Snackbar.LENGTH_SHORT)
            else {
                for (pokemonEntry in pokedexResponse.body()!!.pokemon_entries)
                    names.add(pokemonEntry.pokemon_species.name)
                Log.i(TAG, "size=" + names.size)
                loadPokemon()
            }
        }
    }

    private fun getType() {
        lifecycleScope.launch(Dispatchers.IO) {
            val typeResponse = pokemonApi.getType(typeID)
            if (!typeResponse.isSuccessful)
                showSnackbarInMain(binding.root, "Failed to fetch Type", Snackbar.LENGTH_SHORT)
            else {
                for (typePokemon in typeResponse.body()!!.pokemon)
                    names.add(typePokemon.pokemon.name)
                loadPokemon()
            }
        }
    }

    private fun loadPokemon() {
        val toIndex: Int
        if (offset + 20 > names.size - 1) {
            toIndex = names.size - 1
            paginate = false
        } else toIndex = offset + 20
        val subList = names.subList(offset, toIndex)
        lifecycleScope.launch(Dispatchers.IO) {
            val deferredList = ArrayList<Deferred<Pokemon>>()
            for (s in subList)
                deferredList.add(async { pokemonApi.getPokemon(s).body()!! })
            pokemons.addAll(deferredList.awaitAll())
            withContext(Dispatchers.Main) {
                if (!searching) {
                    binding.pokemonsList.setHasFixedSize(false)
                    pokemonAdapter.notifyItemRangeInserted(pokemons.size - subList.size, subList.size)
                    binding.pokemonsList.setHasFixedSize(true)
                } else
                    searchPokemonByName(searchView.query.toString().trim().toLowerCase(Locale.ROOT))
            }
        }
    }

    private fun searchPokemonByName(name: String) {
        searchedPokemon.clear()
        for (pokemon in pokemons)
            if (pokemon.name.trim().contains(name)) searchedPokemon.add(pokemon)
        pokemonAdapter.setPokemons(searchedPokemon)
        pokemonAdapter.notifyDataSetChanged()
    }

    private fun showDetails(position: Int, pokemonIv: ImageView, nameIv: TextView) {
        val pokemon = pokemonAdapter.getPokemonAt(position)
        if (pokemon.id != 0 && pokemon.sprites != null) {
            val intent = Intent(this@PokemonsActivity, PokemonDetailsActivity::class.java)
            val gson = Gson()
            val pokemonJson = gson.toJson(pokemon)
            intent.putExtra("pokemonJson", pokemonJson)
            val imagePair = Pair<View, String>(pokemonIv, "pokemonImg")
            val namePair = Pair<View, String>(nameIv, "pokemonName")
            val options = ActivityOptions.makeSceneTransitionAnimation(this@PokemonsActivity, imagePair, namePair)
            startActivity(intent, options.toBundle())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Pokémon"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(TAG, "submit")
                searching = true
                if (query.length == 0) {
                    searchedPokemon.clear()
                    pokemonAdapter.setPokemons(pokemons)
                    pokemonAdapter.notifyDataSetChanged()
                } else searchPokemonByName(query.toLowerCase(Locale.ROOT).trim())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searching = true
                if (newText.isEmpty()) {
                    searchedPokemon.clear()
                    pokemonAdapter.setPokemons(pokemons)
                    pokemonAdapter.notifyDataSetChanged()
                } else searchPokemonByName(newText.toLowerCase(Locale.ROOT).trim())
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "PokemonsActivity"
    }
}