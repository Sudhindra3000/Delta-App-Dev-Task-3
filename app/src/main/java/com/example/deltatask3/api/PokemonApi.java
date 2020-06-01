package com.example.deltatask3.api;

import com.example.deltatask3.utils.EvolutionChain;
import com.example.deltatask3.utils.ItemLocation;
import com.example.deltatask3.utils.Pokedex;
import com.example.deltatask3.utils.Pokemon;
import com.example.deltatask3.utils.PokemonID;
import com.example.deltatask3.utils.PokemonSpecies;
import com.example.deltatask3.utils.Region;
import com.example.deltatask3.utils.SearchResult;
import com.example.deltatask3.utils.Type;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokemonApi {

    @GET("https://pokeapi.co/api/v2/pokemon/")
    Call<SearchResult> getPokemonWithOffsetAndLimit(@Query("offset") int offset, @Query("limit") int limit);

    @GET("https://pokeapi.co/api/v2/pokemon/{name}/")
    Call<Pokemon> getPokemonFromName(@Path("name") String name);

    @GET("https://pokeapi.co/api/v2/pokemon-species/{id}/")
    Call<PokemonSpecies> getSpeciesFromID(@Path("id") int id);

    @GET("https://pokeapi.co/api/v2/evolution-chain/{id}")
    Call<EvolutionChain> getEvolutionChainFromID(@Path("id") String idPlusSlash);

    @GET("https://pokeapi.co/api/v2/pokemon/{name}/")
    Call<PokemonID> getPokemonIDFromName(@Path("name") String name);

    @GET("https://pokeapi.co/api/v2/region/{id}/")
    Call<Region> getRegionFromID(@Path("id") int id);

    @GET("https://pokeapi.co/api/v2/pokedex/{name}/")
    Call<Pokedex> getPokedexFromName(@Path("name") String name);

    @GET("https://pokeapi.co/api/v2/type/{id}/")
    Call<Type> getTypeFromID(@Path("id") int id);

    @GET("https://pokeapi.co/api/v2/item/")
    Call<SearchResult> getItemsWithOffsetAndLimit(@Query("offset") int offset, @Query("limit") int limit);

    @GET("https://pokeapi.co/api/v2/item/{name}/")
    Call<ItemLocation> getItemFromName(@Path("name") String name);

    @GET("https://pokeapi.co/api/v2/location/")
    Call<SearchResult> getLocationsWithOffsetAndLimit(@Query("offset") int offset, @Query("limit") int limit);

    @GET("https://pokeapi.co/api/v2/location/{name}/")
    Call<ItemLocation> getLocationFromName(@Path("name") String name);

    @GET("https://pokeapi.co/api/v2/pokemon/{id}/")
    Call<Pokemon> getPokemonFromID(@Path("id") int id);

    @GET("https://pokeapi.co/api/v2/item/{id}/")
    Call<ItemLocation> getItemFromID(@Path("id") int id);

    @GET("https://pokeapi.co/api/v2/location/{id}/")
    Call<ItemLocation> getLocationFromID(@Path("id") int id);
}
