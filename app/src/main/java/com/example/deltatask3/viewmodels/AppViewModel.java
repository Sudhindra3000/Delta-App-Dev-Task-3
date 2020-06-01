package com.example.deltatask3.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    private MutableLiveData<String> currentTitle= new MutableLiveData<>();

    public MutableLiveData<String> getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(String currentTitle) {
        this.currentTitle.setValue(currentTitle);
    }
}
