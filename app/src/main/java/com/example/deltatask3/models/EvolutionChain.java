package com.example.deltatask3.models;

import java.util.List;

public class EvolutionChain {

    private int id;
    private ChainLink chain;

    public class ChainLink{
        private PokemonSpecies species;
        private List<ChainLink> evolves_to;

        public PokemonSpecies getSpecies() {
            return species;
        }

        public List<ChainLink> getEvolves_to() {
            return evolves_to;
        }
    }

    public int getId() {
        return id;
    }

    public ChainLink getChain() {
        return chain;
    }
}
