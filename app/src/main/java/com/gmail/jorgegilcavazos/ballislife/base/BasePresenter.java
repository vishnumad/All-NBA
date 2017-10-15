package com.gmail.jorgegilcavazos.ballislife.base;

import android.support.annotation.NonNull;

public abstract class BasePresenter<V> {

    protected V view;

    public void attachView(@NonNull V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    protected final boolean isViewAttached() {
        return view != null;
    }
}
