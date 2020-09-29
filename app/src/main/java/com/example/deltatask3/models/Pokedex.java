package com.example.deltatask3.models;

import java.util.List;

public class Pokedex {

    private String name;
    private List<PokemonEntry> pokemon_entries;

    public class PokemonEntry{
        private PokemonSpecies pokemon_species;

        public PokemonSpecies getPokemon_species() {
            return pokemon_species;
        }
    }

    public String getName() {
        return name;
    }

    public List<PokemonEntry> getPokemon_entries() {
        return pokemon_entries;
    }
}
