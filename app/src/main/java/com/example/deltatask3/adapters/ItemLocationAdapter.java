package com.example.deltatask3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltatask3.R;
import com.example.deltatask3.databinding.PokemonRowBinding;
import com.example.deltatask3.utils.ItemLocation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.deltatask3.UtilsKt.firstLetterToUppercase;

public class ItemLocationAdapter extends RecyclerView.Adapter<ItemLocationAdapter.ItemLocationViewHolder> {

    private ArrayList<ItemLocation> itemLocations;

    public void setItemLocations(ArrayList<ItemLocation> itemLocations) {
        this.itemLocations = itemLocations;
    }

    public static class ItemLocationViewHolder extends RecyclerView.ViewHolder {

        PokemonRowBinding binding;

        public ItemLocationViewHolder(@NonNull PokemonRowBinding pokemonRowBinding) {
            super(pokemonRowBinding.getRoot());
            binding = pokemonRowBinding;
        }
    }

    @NonNull
    @Override
    public ItemLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PokemonRowBinding binding = PokemonRowBinding.inflate(layoutInflater, parent, false);
        return new ItemLocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemLocationViewHolder holder, int position) {
        ItemLocation itemLocation = itemLocations.get(position);
        if (itemLocation == null) return;

        if (itemLocation.getSprite() != null) {
            holder.binding.ivPokemon.setVisibility(View.VISIBLE);
            String urlSprite = itemLocation.getSprite().getDefault_sprite();
            if (urlSprite != null && !urlSprite.isEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivPokemon);
        } else
            holder.binding.ivPokemon.setVisibility(View.INVISIBLE);
        holder.binding.tvId.setText(String.valueOf(itemLocation.getId()));
        holder.binding.tvName.setText(firstLetterToUppercase(itemLocation.getName()));
    }

    @Override
    public int getItemCount() {
        return itemLocations.size();
    }
}
