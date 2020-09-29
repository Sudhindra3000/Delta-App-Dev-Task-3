package com.example.deltatask3.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
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
import com.example.deltatask3.adapters.ItemLocationAdapter
import com.example.deltatask3.adapters.PokemonAdapter
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.databinding.FragmentSearchBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.ItemLocation
import com.example.deltatask3.models.Pokemon
import com.example.deltatask3.viewmodels.FavouriteViewModel
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        private const val TAG = "SearchFragment"
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var favouriteViewModel: FavouriteViewModel? = null

    private var searchJob: Job? = null

    @JvmField
    @Inject
    var pokemonApi: PokemonApi? = null
    private val pokemons = ArrayList<Pokemon?>()
    private val locations = ArrayList<ItemLocation?>()
    private val items = ArrayList<ItemLocation?>()
    private var pokemonAdapter: PokemonAdapter? = null
    private var locationAdapter: ItemLocationAdapter? = null
    private var itemAdapter: ItemLocationAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favouriteViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(FavouriteViewModel::class.java)
        buildRecyclerView()
    }

    private fun buildRecyclerView() {
        binding.pokemonCard.setHasFixedSize(true)
        val pokemonLayoutManager = LinearLayoutManager(requireContext())
        pokemonAdapter = PokemonAdapter()
        pokemonAdapter!!.setPokemons(pokemons)
        pokemonAdapter!!.setListener { _: Int, pokemon: ImageView, name: TextView -> showDetails(pokemon, name) }
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (pokemonIsInFavourites(pokemons[0])) {
                    pokemonAdapter!!.notifyItemChanged(0)
                    StyleableToast.makeText(requireContext(), firstLetterToUppercase(pokemons[0]!!.name) + " is already in favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
                } else {
                    StyleableToast.makeText(requireContext(), firstLetterToUppercase(pokemons[0]!!.name) + " added to favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
                    favouriteViewModel!!.insert(Favourite(pokemons[0]))
                    pokemons.clear()
                    binding.pokemonCard.visibility = View.GONE
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
        }).attachToRecyclerView(binding.pokemonCard)
        binding.pokemonCard.layoutManager = pokemonLayoutManager
        binding.pokemonCard.adapter = pokemonAdapter
        binding.itemCard.setHasFixedSize(true)
        val itemLayoutManager = LinearLayoutManager(requireContext())
        itemAdapter = ItemLocationAdapter()
        itemAdapter!!.setItemLocations(items)
        binding.itemCard.layoutManager = itemLayoutManager
        binding.itemCard.adapter = itemAdapter
        binding.locationCard.setHasFixedSize(true)
        val locationLayoutManager = LinearLayoutManager(requireContext())
        locationAdapter = ItemLocationAdapter()
        locationAdapter!!.setItemLocations(locations)
        binding.locationCard.layoutManager = locationLayoutManager
        binding.locationCard.adapter = locationAdapter
    }

    private fun pokemonIsInFavourites(pokemon: Pokemon?): Boolean {
        for (favourite in favouriteViewModel!!.allFavourites.value!!) {
            if (favourite.pokemon.id == pokemon!!.id) return true
        }
        return false
    }

    private fun showDetails(pokemonIv: ImageView, nameIv: TextView) {
        if (pokemons[0]!!.id != 0 && pokemons[0]!!.sprites != null) {
            val intent = Intent(requireActivity(), PokemonDetailsActivity::class.java)
            val gson = Gson()
            val pokemonJson = gson.toJson(pokemons[0])
            intent.putExtra("pokemonJson", pokemonJson)
            val imagePair = Pair<View, String>(pokemonIv, "pokemonImg")
            val namePair = Pair<View, String>(nameIv, "pokemonName")
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair)
            startActivity(intent, options.toBundle())
        }
    }

    private fun search(id: Int) {
        if (searchJob != null && searchJob!!.isActive)
            searchJob!!.cancel()
        searchJob = lifecycleScope.launch(Dispatchers.IO) {
            val pokemonCall = async { pokemonApi!!.getPokemon(id) }
            val itemCall = async { pokemonApi!!.getItem(id) }
            val locationCall = async { pokemonApi!!.getLocation(id) }
            pokemons.clear()
            items.clear()
            locations.clear()
            pokemons.add(pokemonCall.await().body())
            items.add(itemCall.await().body())
            locations.add(locationCall.await().body())
            withContext(Dispatchers.Main) {
                binding.searchResults.visibility = View.VISIBLE
                binding.pokemonCard.visibility = View.VISIBLE
                binding.itemCard.visibility = View.VISIBLE
                binding.locationCard.visibility = View.VISIBLE
                pokemonAdapter!!.notifyDataSetChanged()
                itemAdapter!!.notifyDataSetChanged()
                locationAdapter!!.notifyDataSetChanged()
                searchJob = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Enter ID"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.inputType = InputType.TYPE_CLASS_NUMBER
        searchView.setOnCloseListener {
            binding.tvS.visibility = View.VISIBLE
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchResults.visibility = View.GONE
                if (query.isNotEmpty())
                    search(query.trim().toInt())
                binding.tvS.visibility = View.INVISIBLE
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                binding.searchResults.visibility = View.GONE
                if (newText.isNotEmpty())
                    search(newText.trim().toInt())
                binding.tvS.visibility = View.INVISIBLE
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