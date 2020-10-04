package com.example.deltatask3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.ItemLocationAdapter.ItemLocationViewHolder
import com.example.deltatask3.databinding.PokemonRowBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.ItemLocation
import com.squareup.picasso.Picasso

class ItemLocationAdapter(var itemLocations: ArrayList<ItemLocation>) : RecyclerView.Adapter<ItemLocationViewHolder>() {

    class ItemLocationViewHolder(var binding: PokemonRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PokemonRowBinding.inflate(layoutInflater, parent, false)
        return ItemLocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemLocationViewHolder, position: Int) {
        val itemLocation = itemLocations[position]
        if (itemLocation.sprite != null) {
            holder.binding.ivPokemon.visibility = View.VISIBLE
            val urlSprite = itemLocation.sprite.default_sprite
            if (urlSprite != null && urlSprite.isNotEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivPokemon)
        } else holder.binding.ivPokemon.visibility = View.INVISIBLE
        holder.binding.tvId.text = itemLocation.id.toString()
        holder.binding.tvName.text = firstLetterToUppercase(itemLocation.name)
    }

    override fun getItemCount() = itemLocations.size
}