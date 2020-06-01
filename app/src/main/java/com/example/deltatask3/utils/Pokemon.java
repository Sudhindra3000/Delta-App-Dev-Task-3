package com.example.deltatask3.utils;

import java.util.List;

public class Pokemon {

    private int id;
    private String name;
    private int height, weight;
    private Sprites sprites;
    private PokemonSpecies species;
    private List<PokemonStat> stats;
    private List<PokemonType> types;

    public Pokemon() {

    }

    public Pokemon(int id) {
        this.id = id;
    }

    public Pokemon(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public PokemonSpecies getSpecies() {
        return species;
    }

    public List<PokemonStat> getStats() {
        return stats;
    }

    public List<PokemonType> getTypes() {
        return types;
    }
}
