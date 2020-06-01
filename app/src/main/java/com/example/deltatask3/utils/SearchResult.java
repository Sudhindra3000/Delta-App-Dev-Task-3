package com.example.deltatask3.utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResult {

    private int count;
    @SerializedName("next")
    private String nextURL;
    @SerializedName("previous")
    private String previousURL;
    private List<Result> results;

    public class Result{
        private String name,url;

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }
    }

    public List<Result> getResults() {
        return results;
    }
}
