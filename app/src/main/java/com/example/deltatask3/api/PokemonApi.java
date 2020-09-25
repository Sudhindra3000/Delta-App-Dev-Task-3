package com.example.deltatask3.api;

import com.example.deltatask3.utils.EvolutionChain;
import com.example.deltatask3.utils.ItemLocation;
import com.example.deltatask3.utils.Pokedex;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.PokemonId;
import com.example.deltatask3.utils.PokemonSpecies;
import com.example.deltatask3.utils.Region;
import com.example.deltatask3.utils.SearchResult;
import com.example.deltatask3.utils.Type;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokemonApi {

    @GET("pokemon")
    Call<SearchResult> getPokemon(@Query("offset") int offset, @Query("limit") int limit);

    @GET("pokemon/{name}/")
    Call<Pokemon> getPokemon(@Path("name") String name);

    @GET("pokemon/{id}/")
    Call<Pokemon> getPokemon(@Path("id") int id);

    @GET("pokemon-species/{id}/")
    Call<PokemonSpecies> getSpecies(@Path("id") int id);

    @GET("evolution-chain/{id}")
    Call<EvolutionChain> getEvolutionChain(@Path("id") String idPlusSlash);

    @GET("pokemon/{name}/")
    Call<PokemonId> getPokemonId(@Path("name") String name);

    @GET("region/{id}/")
    Call<Region> getRegion(@Path("id") int id);

    @GET("pokedex/{name}/")
    Call<Pokedex> getPokedex(@Path("name") String name);

    @GET("type/{id}/")
    Call<Type> getType(@Path("id") int id);

    @GET("item")
    Call<SearchResult> getItems(@Query("offset") int offset, @Query("limit") int limit);

    @GET("item/{name}/")
    Call<ItemLocation> getItem(@Path("name") String name);

    @GET("item/{id}/")
    Call<ItemLocation> getItem(@Path("id") int id);

    @GET("location")
    Call<SearchResult> getLocations(@Query("offset") int offset, @Query("limit") int limit);

    @GET("location/{name}/")
    Call<ItemLocation> getLocation(@Path("name") String name);

    @GET("location/{id}/")
    Call<ItemLocation> getLocation(@Path("id") int id);
}
