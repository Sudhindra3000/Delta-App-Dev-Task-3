package com.example.deltatask3.models;

public class PokemonStat {

    private Stat stat;
    private int base_stat;

    public class Stat{
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public int getBase_stat() {
        return base_stat;
    }

    public Stat getStat() {
        return stat;
    }
}
