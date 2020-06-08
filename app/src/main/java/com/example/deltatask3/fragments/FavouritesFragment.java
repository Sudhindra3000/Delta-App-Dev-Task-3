package com.example.deltatask3.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deltatask3.activities.PokemonDetailsActivity;
import com.example.deltatask3.adapters.FavouriteAdapter;
import com.example.deltatask3.database.Favourite;
import com.example.deltatask3.databinding.FragmentFavouritesBinding;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.viewmodels.AppViewModel;
import com.example.deltatask3.R;
import com.example.deltatask3.viewmodels.FavouriteViewModel;
import com.google.gson.Gson;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class FavouritesFragment extends Fragment {

    private static final String TAG = "FavouritesFragment";
    private AppViewModel appViewModel;
    private FragmentFavouritesBinding binding;
    private FavouriteViewModel favouriteViewModel;
    private ArrayList<Favourite> searchedFavourites;
    private FavouriteAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SearchView searchView;
    private int removedPos;
    private boolean searching = false, removed = false;


    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        appViewModel.setCurrentTitle("Favourites");
        favouriteViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(FavouriteViewModel.class);
        searchedFavourites = new ArrayList<>();

        buildRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favouriteViewModel.getAllFavourites().observe(getViewLifecycleOwner(), new Observer<List<Favourite>>() {
            @Override
            public void onChanged(List<Favourite> favourites) {
                if (favourites.isEmpty()) {
                    binding.tvFDescription.setVisibility(View.VISIBLE);
                    binding.favourites.setVisibility(View.INVISIBLE);
                    setHasOptionsMenu(false);
                } else {
                    binding.tvFDescription.setVisibility(View.INVISIBLE);
                    binding.favourites.setVisibility(View.VISIBLE);
                    setHasOptionsMenu(true);
                }
                if (searching)
                    adapter.setFavourites(searchedFavourites);
                else
                    adapter.setFavourites(favourites);
                if (removed)
                    adapter.notifyItemRemoved(removedPos);
                else
                    adapter.notifyDataSetChanged();
                removed = false;
            }
        });
    }

    private void buildRecyclerView() {
        binding.favourites.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(requireContext());
        adapter = new FavouriteAdapter();
        adapter.setListener(new FavouriteAdapter.FavouriteListener() {
            @Override
            public void onItemClicked(int pos, ImageView pokemon, TextView name) {
                showDetails(adapter.getFavouriteAt(pos).getPokemon(), pokemon, name);
            }

            @Override
            public void onShareClicked(int pos, ImageView imageView) {
                sharePokemon(pos, imageView);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Favourite favourite = adapter.getFavouriteAt(viewHolder.getAdapterPosition());
                removeFromFav(favourite, viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.parseColor("#EB3939"))
                        .addSwipeLeftActionIcon(R.drawable.delete_icon)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.favourites);
        binding.favourites.setLayoutManager(layoutManager);
        binding.favourites.setAdapter(adapter);
    }

    private void removeFromFav(Favourite favourite, int pos) {
        Log.i(TAG, "removeFromFav: id=" + favourite.getId());
        favouriteViewModel.delete(favourite);
        if (searching)
            searchedFavourites.remove(pos);
        StyleableToast.makeText(requireContext(), firstLetterToUppercase(favourite.getPokemon().getName()) + " is removed from Favourites", Toast.LENGTH_SHORT, R.style.ToastTheme).show();
        removedPos = pos;
        removed = true;
    }

    private void sharePokemon(int pos, ImageView imageView) {
        Pokemon pokemon = adapter.getFavouriteAt(pos).getPokemon();
        Bitmap bitmap = getBitmapFromView(imageView);
        try {
            File file = new File(requireActivity().getExternalCacheDir(), firstLetterToUppercase(pokemon.getName()) + ".png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            file.setReadable(true, false);
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getPokemonDetailsAsString(pokemon));
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share Pokémon via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPokemonDetailsAsString(Pokemon pokemon) {
        String details = "ID : " + pokemon.getId() + "\n" +
                "Pokémon : " + firstLetterToUppercase(pokemon.getName()) + "\n";
        if (pokemon.getTypes().size() == 1)
            details = details + "Type : " + firstLetterToUppercase(pokemon.getTypes().get(0).getType().getName()) + "\n";
        else
            details = details + "Types : " + firstLetterToUppercase(pokemon.getTypes().get(0).getType().getName()) + ", " + firstLetterToUppercase(pokemon.getTypes().get(1).getType().getName()) + "\n";
        details = details + "Speed : " + pokemon.getStats().get(0).getBase_stat() + "\n"
                + "Hp : " + pokemon.getStats().get(5).getBase_stat() + "\n"
                + "Attack : " + pokemon.getStats().get(4).getBase_stat() + "\n"
                + "Defense : " + pokemon.getStats().get(3).getBase_stat() + "\n"
                + "Sp. Attack : " + pokemon.getStats().get(2).getBase_stat() + "\n"
                + "Sp. Defense : " + pokemon.getStats().get(1).getBase_stat();
        return details;
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void showDetails(Pokemon pokemon, ImageView pokemonIv, TextView nameIv) {
        if (pokemon.getId() != 0 && pokemon.getSprites() != null) {
            Intent intent = new Intent(requireActivity(), PokemonDetailsActivity.class);
            Gson gson = new Gson();
            String pokemonJson = gson.toJson(pokemon);
            intent.putExtra("pokemonJson", pokemonJson);
            Pair<View, String> imagePair = new Pair<>(pokemonIv, "pokemonImg");
            Pair<View, String> namePair = new Pair<>(nameIv, "pokemonName");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagePair, namePair);
            startActivity(intent, options.toBundle());
        }
    }

    private void searchFavouritesByName(String name) {
        searchedFavourites.clear();

        Favourite favouriteS;
        for (Favourite favourite : favouriteViewModel.getAllFavourites().getValue()) {
            if (favourite.getPokemon().getName().trim().contains(name)) {
                favouriteS = new Favourite(favourite.getPokemon());
                favouriteS.setId(favourite.getId());
                searchedFavourites.add(favouriteS);
            }
        }

        adapter.setFavourites(searchedFavourites);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.favourites_menu, menu);
        MenuItem item = menu.findItem(R.id.searchFavourites);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Favourites");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searching = false;
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = true;
                if (query.length() == 0) {
                    searchedFavourites.clear();
                    adapter.setFavourites(favouriteViewModel.getAllFavourites().getValue());
                    adapter.notifyDataSetChanged();
                } else
                    searchFavouritesByName(query.toLowerCase().trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                if (newText.length() == 0) {
                    searchedFavourites.clear();
                    adapter.setFavourites(favouriteViewModel.getAllFavourites().getValue());
                    adapter.notifyDataSetChanged();
                } else
                    searchFavouritesByName(newText.toLowerCase().trim());
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                favouriteViewModel.deleteAllFavourites();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private String firstLetterToUppercase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
