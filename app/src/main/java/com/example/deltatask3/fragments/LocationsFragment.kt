package com.example.deltatask3.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.ItemLocationAdapter
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.databinding.FragmentLocationsBinding
import com.example.deltatask3.models.ItemLocation
import com.example.deltatask3.showSnackbarInMain
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class LocationsFragment : Fragment() {

    companion object {
        private const val TAG = "LocationsFragment"
    }

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pokemonApi: PokemonApi

    private val locations = ArrayList<ItemLocation>()
    private val searchedLocations = ArrayList<ItemLocation>()
    private val adapter = ItemLocationAdapter(locations)
    private lateinit var layoutManager: LinearLayoutManager

    private var offset = 0
    private var loading = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildRecyclerView()
        getLocations()
    }

    private fun buildRecyclerView() {
        binding.allLocations.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext())
        binding.allLocations.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.allLocations.layoutManager = layoutManager
        binding.allLocations.adapter = adapter
    }

    private fun paginate() {
        offset += 20
        getLocations()
    }

    private fun getLocations() {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = pokemonApi.getLocations(offset, 20)
            if (!response.isSuccessful)
                showSnackbarInMain(requireView(), "Failed to Fetch Locations", Snackbar.LENGTH_SHORT)
            else {
                val results = response.body()!!.results
                val deferredList = ArrayList<Deferred<ItemLocation>>()
                for (result in results)
                    deferredList.add(async { pokemonApi.getLocation(result.name).body()!! })
                locations.addAll(deferredList.awaitAll())
                loading = true
                withContext(Dispatchers.Main) {
                    binding.allLocations.setHasFixedSize(false)
                    adapter.notifyItemRangeInserted(locations.size - results.size, results.size)
                    binding.allLocations.setHasFixedSize(true)
                }
            }
        }
    }

    private fun searchLocationsByName(name: String) {
        searchedLocations.clear()
        for (item in locations)
            if (item.name.trim().contains(name)) searchedLocations.add(item)
        adapter.itemLocations = searchedLocations
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Locations"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    searchedLocations.clear()
                    adapter.itemLocations = locations
                    adapter.notifyDataSetChanged()
                } else searchLocationsByName(newText.toLowerCase(Locale.ROOT).trim())
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}