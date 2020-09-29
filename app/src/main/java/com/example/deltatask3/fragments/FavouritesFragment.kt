package com.example.deltatask3.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.activities.PokemonDetailsActivity
import com.example.deltatask3.adapters.FavouriteAdapter
import com.example.deltatask3.adapters.FavouriteAdapter.FavouriteListener
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.databinding.FragmentFavouritesBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.Pokemon
import com.example.deltatask3.viewmodels.FavouriteViewModel
import com.google.gson.Gson
import com.muddzdev.styleabletoast.StyleableToast
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.io.File
import java.io.FileOutputStream
import java.util.*

@AndroidEntryPoint
class FavouritesFragment : Fragment() {

    companion object {
        private const val TAG = "FavouritesFragment"
    }

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private var favouriteViewModel: FavouriteViewModel? = null
    private val searchedFavourites = ArrayList<Favourite>()
    private var adapter: FavouriteAdapter? = null
    private var removedPos = 0
    private var searching = false
    private var removed = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favouriteViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(FavouriteViewModel::class.java)
        buildRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteViewModel!!.allFavourites.observe(viewLifecycleOwner, { favourites: List<Favourite?> ->
            if (favourites.isEmpty()) {
                binding.tvFDescription.visibility = View.VISIBLE
                binding.favourites.visibility = View.INVISIBLE
                setHasOptionsMenu(false)
            } else {
                binding.tvFDescription.visibility = View.INVISIBLE
                binding.favourites.visibility = View.VISIBLE
                setHasOptionsMenu(true)
            }
            if (searching) adapter!!.setFavourites(searchedFavourites) else adapter!!.setFavourites(favourites)
            if (removed) adapter!!.notifyItemRemoved(removedPos) else adapter!!.notifyDataSetChanged()
            removed = false
        })
    }

    private fun buildRecyclerView() {
        binding.favourites.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        adapter = FavouriteAdapter()
        adapter!!.setListener(object : FavouriteListener {
            override fun onItemClicked(pos: Int, pokemon: ImageView, name: TextView) {
                showDetails(adapter!!.getFavouriteAt(pos).pokemon, pokemon, name)
            }

            override fun onShareClicked(pos: Int, imageView: ImageView) {
                sharePokemon(pos, imageView)
            }
        })
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val favourite = adapter!!.getFavouriteAt(viewHolder.adapterPosition)
                removeFromFav(favourite, viewHolder.adapterPosition)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.parseColor("#EB3939"))
                        .addSwipeLeftActionIcon(R.drawable.delete_icon)
                        .create()
                        .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(binding.favourites)
        binding.favourites.layoutManager = layoutManager
        binding.favourites.adapter = adapter
    }

    private fun removeFromFav(favourite: Favourite, pos: Int) {
        Log.i(TAG, "removeFromFav: id=" + favourite.id)
        favouriteViewModel!!.delete(favourite)
        if (searching) searchedFavourites.removeAt(pos)
        StyleableToast.makeText(requireContext(), firstLetterToUppercase(favourite.pokemon.name) + " is removed from Favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show()
        removedPos = pos
        removed = true
    }

    private fun sharePokemon(pos: Int, imageView: ImageView) {
        val pokemon = adapter!!.getFavouriteAt(pos).pokemon
        val bitmap = getBitmapFromView(imageView)
        try {
            val file = File(requireActivity().externalCacheDir, firstLetterToUppercase(pokemon.name) + ".png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.putExtra(Intent.EXTRA_TEXT, getPokemonDetailsAsString(pokemon))
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file))
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Share Pokémon via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPokemonDetailsAsString(pokemon: Pokemon): String {
        var details = """
            ID : ${pokemon.id}
            Pokémon : ${firstLetterToUppercase(pokemon.name)}

            """.trimIndent()
        details = if (pokemon.types.size == 1) """
     ${details}Type : ${firstLetterToUppercase(pokemon.types[0].type.name)}

     """.trimIndent() else """
     ${details}Types : ${firstLetterToUppercase(pokemon.types[0].type.name)}, ${firstLetterToUppercase(pokemon.types[1].type.name)}

     """.trimIndent()
        details = """
            ${details}Speed : ${pokemon.stats[0].base_stat}
            Hp : ${pokemon.stats[5].base_stat}
            Attack : ${pokemon.stats[4].base_stat}
            Defense : ${pokemon.stats[3].base_stat}
            Sp. Attack : ${pokemon.stats[2].base_stat}
            Sp. Defense : ${pokemon.stats[1].base_stat}
            """.trimIndent()
        return details
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private fun showDetails(pokemon: Pokemon, pokemonIv: ImageView, nameIv: TextView) {
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

    private fun searchFavouritesByName(name: String) {
        searchedFavourites.clear()
        var favouriteS: Favourite
        for (favourite in favouriteViewModel!!.allFavourites.value!!) {
            if (favourite.pokemon.name.trim().contains(name)) {
                favouriteS = Favourite(favourite.pokemon)
                favouriteS.id = favourite.id
                searchedFavourites.add(favouriteS)
            }
        }
        adapter!!.setFavourites(searchedFavourites)
        adapter!!.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.favourites_menu, menu)
        val item = menu.findItem(R.id.searchFavourites)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Favourites"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnCloseListener {
            searching = false
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searching = true
                if (query.isEmpty()) {
                    searchedFavourites.clear()
                    adapter!!.setFavourites(favouriteViewModel!!.allFavourites.value)
                    adapter!!.notifyDataSetChanged()
                } else searchFavouritesByName(query.toLowerCase().trim())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searching = true
                if (newText.isEmpty()) {
                    searchedFavourites.clear()
                    adapter!!.setFavourites(favouriteViewModel!!.allFavourites.value)
                    adapter!!.notifyDataSetChanged()
                } else searchFavouritesByName(newText.toLowerCase().trim())
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteAll -> favouriteViewModel!!.deleteAllFavourites()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}