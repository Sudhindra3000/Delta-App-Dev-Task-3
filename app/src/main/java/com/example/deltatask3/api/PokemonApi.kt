package com.example.deltatask3.api

import com.example.deltatask3.utils.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon")
    suspend fun getPokemon(@Query("offset") offset: Int, @Query("limit") limit: Int): Response<SearchResult>

    @GET("pokemon/{name}/")
    suspend fun getPokemon(@Path("name") name: String): Response<Pokemon>

    @GET("pokemon/{id}/")
    fun getPokemon(@Path("id") id: Int): Call<Pokemon>

    @GET("pokemon-species/{id}/")
    fun getSpecies(@Path("id") id: Int): Call<PokemonSpecies>

    @GET("evolution-chain/{id}")
    fun getEvolutionChain(@Path("id") idPlusSlash: String): Call<EvolutionChain>

    @GET("pokemon/{name}/")
    fun getPokemonId(@Path("name") name: String): Call<PokemonId>

    @GET("region/{id}/")
    fun getRegion(@Path("id") id: Int): Call<Region>

    @GET("pokedex/{name}/")
    fun getPokedex(@Path("name") name: String): Call<Pokedex>

    @GET("type/{id}/")
    fun getType(@Path("id") id: Int): Call<Type>

    @GET("item")
    suspend fun getItems(@Query("offset") offset: Int, @Query("limit") limit: Int): Response<SearchResult>

    @GET("item/{name}/")
    suspend fun getItem(@Path("name") name: String): Response<ItemLocation>

    @GET("item/{id}/")
    fun getItem(@Path("id") id: Int): Call<ItemLocation>

    @GET("location")
    suspend fun getLocations(@Query("offset") offset: Int, @Query("limit") limit: Int): Response<SearchResult>

    @GET("location/{name}/")
    suspend fun getLocation(@Path("name") name: String): Response<ItemLocation>

    @GET("location/{id}/")
    fun getLocation(@Path("id") id: Int): Call<ItemLocation>
}