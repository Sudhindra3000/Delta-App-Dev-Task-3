package com.example.deltatask3.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.deltatask3.models.Pokemon;

@Entity(tableName = "favourites_table")
public class Favourite {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @TypeConverters(PokemonTypeConverter.class)
    private Pokemon pokemon;

    public Favourite(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
