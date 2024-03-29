package com.example.deltatask3.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.activities.PokemonDetailsActivity
import com.example.deltatask3.adapters.PokemonAdapter
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.databinding.FragmentPokemonsBinding
import com.example.deltatask3.firstLetterToUppercase
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

@AndroidEntryPoint
class PokemonsFragment : Fragment() {

    companion object {
        private const val TAG = "PokemonsFragment"
    }

    private var _binding: FragmentPokemonsBinding? = null
    private val binding get() = _binding!!

    private lateinit var favouriteViewModel: FavouriteViewModel

    @Inject
    lateinit var pokemonApi: PokemonApi

    private val names = ArrayList<String>()
    private val allPokemons = ArrayList<Pokemon>()
    private val favourites = ArrayList<Favourite>()
    private val searchedPokemon = ArrayList<Pokemon>()
    private val pokemonAdapter = PokemonAdapter(allPokemons) { position: Int, pokemonIv: ImageView, nameIv: TextView -> showDetails(position, pokemonIv, nameIv) }
    private lateinit var layoutManager: LinearLayoutManager

    private var offset = 0
    private lateinit var searchView: SearchView
    private var loading = true
    private var searching = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPokemonsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favouriteViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(FavouriteViewModel::class.java)
        buildRecyclerView()
        getPokemon()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteViewModel.allFavourites.observe(viewLifecycleOwner, { newFavourites: List<Favourite>? ->
            favourites.clear()
            favourites.addAll(newFavourites!!)
        })
    }

    private fun buildRecyclerView() {
        binding.allPokemonList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext())
        binding.allPokemonList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.allPokemonList.layoutManager = layoutManager
        binding.allPokemonList.adapter = pokemonAdapter
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipedPokemon = pokemonAdapter.getPokemonAt(viewHolder.adapterPosition)
                if (pokemonIsInFavourites(swipedPokemon)) {
                    pokemonAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    StyleableToast.makeText(requireContext(), firstLetterToUppercase(swipedPokemon.name) + " is already in favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
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
        }).attachToRecyclerView(binding.allPokemonList)
    }

    private fun pokemonIsInFavourites(pokemon: Pokemon): Boolean {
        for (favourite in favourites) {
            if (favourite.pokemon.id == pokemon.id) return true
        }
        return false
    }

    private fun addToFavourites(position: Int, pokemon: Pokemon) {
        StyleableToast.makeText(requireContext(), firstLetterToUppercase(pokemon.name) + " added to favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
        favouriteViewModel.insert(Favourite(pokemon))
        names.remove(pokemon.name)
        allPokemons.remove(pokemon)
        if (searching) searchedPokemon.remove(pokemon)
        pokemonAdapter.notifyItemRemoved(position)
    }

    private fun paginate() {
        offset += 20
        getPokemon()
    }

    private fun getPokemon() {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = pokemonApi.getPokemon(offset, 20)
            if (!response.isSuccessful)
                showSnackbarInMain(requireView(), "Failed to Fetch Pokemon", Snackbar.LENGTH_SHORT)
            else {
                val results = response.body()!!.results
                val deferredList = ArrayList<Deferred<Pokemon>>()
                for (result in results)
                    deferredList.add(async { pokemonApi.getPokemon(result.name).body()!! })
                allPokemons.addAll(deferredList.awaitAll())
                loading = true
                withContext(Dispatchers.Main) {
                    binding.allPokemonList.setHasFixedSize(false)
                    pokemonAdapter.notifyItemRangeInserted(allPokemons.size - results.size, results.size)
                    binding.allPokemonList.setHasFixedSize(true)
                }
            }
        }
    }

    private fun searchPokemonByName(name: String) {
        searchedPokemon.clear()
        for (pokemon in allPokemons)
            if (pokemon.name.trim().contains(name)) searchedPokemon.add(pokemon)
        pokemonAdapter.pokemons = searchedPokemon
        pokemonAdapter.notifyDataSetChanged()
    }

    private fun showDetails(position: Int, pokemonIv: ImageView, nameIv: TextView) {
        val pokemon = pokemonAdapter.getPokemonAt(position)
        if (pokemon.id != 0 && pokemon.sprites != null) {
            val intent = Intent(requireActivity(), PokemonDetailsActivity::class.java)
            val gson = Gson()
            val pokemonJson = gson.toJson(pokemon)
            intent.putExtra("pokemonJson", pokemonJson)
            val imagePair = Pair<View, String>(pokemonIv, "pokemonImg")
            val namePair = Pair<View, String>(nameIv, "pokemonName")
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair)
            startActivity(intent, options.toBundle())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search)
        searchView = item.actionView as SearchView
        searchView.queryHint = "Search Pokémon"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(TAG, "submit")
                searching = true
                if (query.isEmpty()) {
                    searchedPokemon.clear()
                    pokemonAdapter.pokemons = allPokemons
                    pokemonAdapter.notifyDataSetChanged()
                } else searchPokemonByName(query.toLowerCase(Locale.ROOT).trim())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searching = true
                if (newText.isEmpty()) {
                    searchedPokemon.clear()
                    pokemonAdapter.pokemons = allPokemons
                    pokemonAdapter.notifyDataSetChanged()
                } else searchPokemonByName(newText.toLowerCase(Locale.ROOT).trim())
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}