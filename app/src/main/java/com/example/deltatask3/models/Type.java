package com.example.deltatask3.models;

import java.util.List;

public class Type {

    private List<TypePokemon> pokemon;

    public class TypePokemon{

        private SearchResult.Result pokemon;

        public SearchResult.Result getPokemon() {
            return pokemon;
        }
    }

    public List<TypePokemon> getPokemon() {
        return pokemon;
    }
}
