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
import com.example.deltatask3.databinding.FragmentItemsBinding
import com.example.deltatask3.models.ItemLocation
import com.example.deltatask3.showSnackbarInMain
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ItemsFragment : Fragment() {

    companion object {
        private const val TAG = "ItemsFragment"
    }

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pokemonApi: PokemonApi

    private val items = ArrayList<ItemLocation>()
    private val searchedItems = ArrayList<ItemLocation>()
    private val adapter = ItemLocationAdapter(items)
    private lateinit var layoutManager: LinearLayoutManager

    private var offset = 0
    private var loading = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildRecyclerView()
        getItems()
    }

    private fun buildRecyclerView() {
        binding.allItems.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext())
        binding.allItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.allItems.layoutManager = layoutManager
        binding.allItems.adapter = adapter
    }

    private fun paginate() {
        offset += 20
        getItems()
    }

    private fun getItems() {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = pokemonApi.getItems(offset, 20)
            if (!response.isSuccessful)
                showSnackbarInMain(requireView(), "Failed to Fetch Items", Snackbar.LENGTH_SHORT)
            else {
                val results = response.body()!!.results
                val deferredList = ArrayList<Deferred<ItemLocation>>()
                for (result in results)
                    deferredList.add(async { pokemonApi.getItem(result.name).body()!! })
                items.addAll(deferredList.awaitAll())
                loading = true
                withContext(Dispatchers.Main) {
                    binding.allItems.setHasFixedSize(false)
                    adapter.notifyItemRangeInserted(items.size - results.size, results.size)
                    binding.allItems.setHasFixedSize(true)
                }
            }
        }
    }

    private fun searchItemsByName(name: String) {
        searchedItems.clear()
        for (item in items) {
            if (item.name.trim().contains(name)) searchedItems.add(item)
        }
        adapter.itemLocations = searchedItems
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Items"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    searchedItems.clear()
                    adapter.itemLocations = items
                    adapter.notifyDataSetChanged()
                } else searchItemsByName(newText.toLowerCase(Locale.ROOT).trim())
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