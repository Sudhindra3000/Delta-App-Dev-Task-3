package com.example.deltatask3.database

import androidx.room.TypeConverter
import com.example.deltatask3.models.Pokemon
import com.google.gson.Gson

object PokemonTypeConverter {
    @JvmStatic
    @TypeConverter
    fun stringToPokemon(value: String?): Pokemon =
        if (value == null) Pokemon("Name") else Gson().fromJson(value, Pokemon::class.java)

    @JvmStatic
    @TypeConverter
    fun pokemonToString(pokemon: Pokemon?): String = Gson().toJson(pokemon)
}
