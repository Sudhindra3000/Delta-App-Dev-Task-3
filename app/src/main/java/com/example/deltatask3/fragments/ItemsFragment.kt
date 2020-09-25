package com.example.deltatask3.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.ItemLocationAdapter
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.databinding.FragmentItemsBinding
import com.example.deltatask3.utils.ItemLocation
import com.example.deltatask3.utils.SearchResult
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ItemsFragment : Fragment() {

    companion object {
        private const val TAG = "ItemsFragment"
    }

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    @JvmField
    @Inject
    var pokemonApi: PokemonApi? = null
    private val names = ArrayList<String>()
    private val items = ArrayList<ItemLocation?>()
    private val searchedItems = ArrayList<ItemLocation?>()
    private var adapter: ItemLocationAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
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
        adapter = ItemLocationAdapter()
        adapter!!.setItemLocations(items)
        binding.allItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = layoutManager!!.childCount
                    val totalItemCount = layoutManager!!.itemCount
                    val pastVisibleItems = layoutManager!!.findFirstVisibleItemPosition()
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
        val call = pokemonApi!!.getItems(offset, 20)
        call.enqueue(object : Callback<SearchResult> {
            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    return
                }
                for (result in response.body()!!.results) {
                    names.add(result.name)
                    items.add(ItemLocation(result.name))
                }
                loading = true
                loadItems(names)
            }

            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                Log.i(TAG, "t=" + t.localizedMessage)
            }
        })
    }

    private fun loadItems(names: ArrayList<String>) {
        for (s in names) {
            val call = pokemonApi!!.getItem(s)
            call.enqueue(object : Callback<ItemLocation?> {
                override fun onResponse(call: Call<ItemLocation?>, response: Response<ItemLocation?>) {
                    if (!response.isSuccessful) {
                        Log.i(TAG, "onResponse: $response")
                        return
                    }
                    if (names.indexOf(s) >= 0) items[names.indexOf(s)] = response.body()
                    adapter!!.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<ItemLocation?>, t: Throwable) {
                    Log.i(TAG, "t=" + t.localizedMessage)
                }
            })
        }
    }

    private fun searchItemsByName(name: String) {
        var name = name
        name = name.trim { it <= ' ' }.toLowerCase(Locale.ROOT)
        searchedItems.clear()
        for (item in items) {
            if (item!!.name.trim { it <= ' ' }.contains(name)) searchedItems.add(item)
        }
        adapter!!.setItemLocations(searchedItems)
        adapter!!.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Items"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length == 0) {
                    searchedItems.clear()
                    adapter!!.setItemLocations(items)
                    adapter!!.notifyDataSetChanged()
                } else searchItemsByName(newText.toLowerCase(Locale.ROOT).trim { it <= ' ' })
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