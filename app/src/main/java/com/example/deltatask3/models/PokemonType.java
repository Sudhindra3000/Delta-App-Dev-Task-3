package com.example.deltatask3.models;

public class PokemonType {

    private Type type;
    private int slot;

    public class Type{
        private int id;
        private String name;
        private Generation generation;

        public String getName() {
            return name;
        }
    }

    public Type getType() {
        return type;
    }
}
