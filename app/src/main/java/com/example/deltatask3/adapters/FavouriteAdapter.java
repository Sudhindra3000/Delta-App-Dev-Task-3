package com.example.deltatask3.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deltatask3.R;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FavouriteRowBinding;
import com.example.deltatask3.utils.Pokemon;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder> {

    private List<Favourite> favourites;
    private FavouriteListener listener;

    public void setFavourites(List<Favourite> favourites) {
        this.favourites = favourites;
    }

    public void setListener(FavouriteListener listener) {
        this.listener = listener;
    }

    public interface FavouriteListener {
        void onItemClicked(int pos, ImageView pokemon, TextView name);

        void onShareClicked(int pos, ImageView imageView);
    }

    public static class FavouriteViewHolder extends RecyclerView.ViewHolder {

        FavouriteRowBinding binding;

        public FavouriteViewHolder(@NonNull FavouriteRowBinding favouriteRowBinding, FavouriteListener listener) {
            super(favouriteRowBinding.getRoot());
            binding = favouriteRowBinding;
            binding.favRow.setOnClickListener(v -> listener.onItemClicked(getAdapterPosition(), binding.ivF, binding.tvFName));
            binding.shareButton.setOnClickListener(v -> listener.onShareClicked(getAdapterPosition(), binding.ivF));
        }
    }

    @NonNull
    @Override
    public FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FavouriteRowBinding binding = FavouriteRowBinding.inflate(layoutInflater, parent, false);
        return new FavouriteViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteViewHolder holder, int position) {
        Pokemon pokemon = favourites.get(position).getPokemon();

        if (pokemon.getSprites() != null) {
            String urlSprite = pokemon.getSprites().getFront_default();
            if (urlSprite != null && !urlSprite.isEmpty())
                Picasso.get().load(urlSprite).placeholder(R.drawable.placeholder_image).into(holder.binding.ivF);
        }
        holder.binding.tvFId.setText(String.valueOf(pokemon.getId()));
        holder.binding.tvFName.setText(firstLetterToUppercase(pokemon.getName()));
    }

    @Override
    public int getItemCount() {
        if (favourites == null) return 0;
        return favourites.size();
    }

    private String firstLetterToUppercase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public Favourite getFavouriteAt(int pos) {
        return favourites.get(pos);
    }
}
