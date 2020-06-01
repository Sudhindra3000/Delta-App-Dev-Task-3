package com.example.deltatask3.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltatask3.R;
import com.example.deltatask3.databinding.PokemonRowBinding;
import com.example.deltatask3.utils.Pokemon;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    private ArrayList<Pokemon> pokemons;
    private PokemonAdapterListener listener;

    public void setPokemons(ArrayList<Pokemon> pokemons) {
        this.pokemons = pokemons;
    }

    public void setListener(PokemonAdapterListener listener) {
        this.listener = listener;
    }

    public interface PokemonAdapterListener {
        void onItemClicked(int position, ImageView pokemon, TextView name);
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {

        PokemonRowBinding binding;

        public PokemonViewHolder(@NonNull PokemonRowBinding pokemonRowBinding, PokemonAdapterListener listener) {
            super(pokemonRowBinding.getRoot());
            binding = pokemonRowBinding;
            binding.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(getAdapterPosition(), binding.ivPokemon,binding.tvName);
                }
            });
        }
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PokemonRowBinding binding = PokemonRowBinding.inflate(layoutInflater, parent, false);
        return new PokemonViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon pokemon = pokemons.get(position);

        if (pokemon.getSprites() != null) {
            String urlSprite = pokemon.getSprites().getFront_default();
            if (urlSprite!=null && !urlSprite.isEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivPokemon);
        }
        holder.binding.tvId.setText(String.valueOf(pokemon.getId()));
        holder.binding.tvName.setText(firstLetterToUppercase(pokemon.getName()));
    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }

    private String firstLetterToUppercase(String string){
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }

}
