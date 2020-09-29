package com.example.deltatask3.models;


public class PokemonSpecies {

    private int id;
    private String name;
    private EvolutionChainObj evolution_chain;


    public class EvolutionChainObj{
        String url;

        public String getUrl() {
            return url;
        }
    }

    public EvolutionChainObj getEvolution_chain() {
        return evolution_chain;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
