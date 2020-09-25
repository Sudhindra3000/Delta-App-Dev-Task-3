package com.example.deltatask3.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
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
import com.example.deltatask3.utils.ItemLocation
import com.example.deltatask3.utils.Pokemon
import com.example.deltatask3.viewmodels.FavouriteViewModel
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        pokemonAdapter!!.setListener { position: Int, pokemon: ImageView, name: TextView -> showDetails(pokemon, name) }
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
        val pokemonCall = pokemonApi!!.getPokemonFromID(id)
        val itemCall = pokemonApi!!.getItemFromID(id)
        val locationCall = pokemonApi!!.getLocationFromID(id)
        pokemonCall.enqueue(object : Callback<Pokemon?> {
            override fun onResponse(call: Call<Pokemon?>, response: Response<Pokemon?>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    binding.pokemonCard.visibility = View.GONE
                    return
                }
                binding.pokemonCard.visibility = View.VISIBLE
                pokemons.clear()
                pokemons.add(response.body())
                pokemonAdapter!!.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Pokemon?>, t: Throwable) {
                Log.i(TAG, "t=" + t.localizedMessage)
                binding.pokemonCard.visibility = View.GONE
            }
        })
        itemCall.enqueue(object : Callback<ItemLocation?> {
            override fun onResponse(call: Call<ItemLocation?>, response: Response<ItemLocation?>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    binding.itemCard.visibility = View.GONE
                    return
                }
                binding.itemCard.visibility = View.VISIBLE
                items.clear()
                items.add(response.body())
                itemAdapter!!.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<ItemLocation?>, t: Throwable) {
                Log.i(TAG, "t=" + t.localizedMessage)
                binding.itemCard.visibility = View.GONE
            }
        })
        locationCall.enqueue(object : Callback<ItemLocation?> {
            override fun onResponse(call: Call<ItemLocation?>, response: Response<ItemLocation?>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    binding.locationCard.visibility = View.GONE
                    return
                }
                binding.locationCard.visibility = View.VISIBLE
                locations.clear()
                locations.add(response.body())
                locationAdapter!!.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<ItemLocation?>, t: Throwable) {
                Log.i(TAG, "t=" + t.localizedMessage)
                binding.locationCard.visibility = View.GONE
            }
        })
    }

    private fun firstLetterToUppercase(string: String): String {
        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1)
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
                if (query.isNotEmpty()) search(query.trim { it <= ' ' }.toInt()) else search(0)
                binding.tvS.visibility = View.INVISIBLE
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) search(newText.trim { it <= ' ' }.toInt()) else search(0)
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