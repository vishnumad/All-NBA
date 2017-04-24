package com.gmail.jorgegilcavazos.ballislife.features.standings;

import com.gmail.jorgegilcavazos.ballislife.features.model.Standings;
import com.gmail.jorgegilcavazos.ballislife.data.API.NbaStandingsService;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StandingsPresenter extends MvpBasePresenter<StandingsView> {

    private CompositeDisposable disposables;

    public StandingsPresenter() {
        disposables = new CompositeDisposable();
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            disposables.clear();
        }
    }

    public void loadStandings() {
        getView().setLoadingIndicator(true);
        getView().dismissSnackbar();
        getView().hideStandings();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NbaStandingsService service = retrofit.create(NbaStandingsService.class);

        Single<Standings> standings = service.getStandings("22016");

        disposables.clear();
        disposables.add(standings
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Standings>() {
                    @Override
                    public void onSuccess(Standings standings) {
                        getView().setLoadingIndicator(false);
                        getView().showStandings(standings);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().setLoadingIndicator(false);
                        getView().showSnackbar(true);
                    }
                })
        );
    }

    public void dismissSnackbar() {
        getView().dismissSnackbar();
    }
}
