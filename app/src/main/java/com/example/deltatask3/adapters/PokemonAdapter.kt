package com.example.deltatask3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deltatask3.R
import com.example.deltatask3.adapters.PokemonAdapter.PokemonViewHolder
import com.example.deltatask3.databinding.PokemonRowBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.Pokemon
import com.squareup.picasso.Picasso
import java.util.*

class PokemonAdapter(
        var pokemons: ArrayList<Pokemon>,
        private var listener: PokemonAdapterListener
) : RecyclerView.Adapter<PokemonViewHolder>() {

    fun interface PokemonAdapterListener {
        fun onItemClicked(position: Int, pokemon: ImageView, name: TextView)
    }

    class PokemonViewHolder(var binding: PokemonRowBinding, listener: PokemonAdapterListener?) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.row.setOnClickListener { listener!!.onItemClicked(adapterPosition, binding.ivPokemon, binding.tvName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PokemonRowBinding.inflate(layoutInflater, parent, false)
        return PokemonViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemons[position]
        if (pokemon.sprites != null) {
            val urlSprite = pokemon.sprites.front_default
            if (urlSprite != null && urlSprite.isNotEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivPokemon)
        }
        holder.binding.tvId.text = pokemon.id.toString()
        holder.binding.tvName.text = firstLetterToUppercase(pokemon.name)
    }

    override fun getItemCount() = pokemons.size

    fun getPokemonAt(pos: Int) = pokemons[pos]
}