package com.example.deltatask3.database;

import androidx.room.TypeConverter;

import com.example.deltatask3.utils.Pokemon;
import com.google.gson.Gson;

public class PokemonTypeConverter {

    @TypeConverter
    public static Pokemon stringToPokemon(String value){
        if (value==null) return new Pokemon("Name");
        return new Gson().fromJson(value,Pokemon.class);
    }

    @TypeConverter
    public static String pokemonToString(Pokemon pokemon){
        return new Gson().toJson(pokemon);
    }
}
