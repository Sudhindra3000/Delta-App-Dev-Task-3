package com.example.deltatask3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.FavouriteAdapter.FavouriteViewHolder
import com.example.deltatask3.database.Favourite
import com.example.deltatask3.databinding.FavouriteRowBinding
import com.example.deltatask3.firstLetterToUppercase
import com.squareup.picasso.Picasso

class FavouriteAdapter(
        private var listener: FavouriteListener
) : RecyclerView.Adapter<FavouriteViewHolder>() {

    lateinit var favourites: List<Favourite>

    interface FavouriteListener {
        fun onItemClicked(pos: Int, pokemon: ImageView, name: TextView)

        fun onShareClicked(pos: Int, imageView: ImageView)
    }

    class FavouriteViewHolder(var binding: FavouriteRowBinding, listener: FavouriteListener?) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.favRow.setOnClickListener { listener!!.onItemClicked(adapterPosition, binding.ivF, binding.tvFName) }
            binding.shareButton.setOnClickListener { listener!!.onShareClicked(adapterPosition, binding.ivF) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FavouriteRowBinding.inflate(layoutInflater, parent, false)
        return FavouriteViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val pokemon = favourites[position].pokemon
        if (pokemon.sprites != null) {
            val urlSprite = pokemon.sprites.front_default
            if (urlSprite != null && urlSprite.isNotEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivF)
        }
        holder.binding.tvFId.text = pokemon.id.toString()
        holder.binding.tvFName.text = firstLetterToUppercase(pokemon.name)
    }

    override fun getItemCount() = favourites.size

    fun getFavouriteAt(pos: Int) = favourites[pos]
}