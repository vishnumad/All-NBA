package com.gmail.jorgegilcavazos.ballislife.dagger.component;

import com.gmail.jorgegilcavazos.ballislife.dagger.module.AppModule;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.DataModule;
import com.gmail.jorgegilcavazos.ballislife.data.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.local.AppLocalRepository;
import com.gmail.jorgegilcavazos.ballislife.features.highlights.HighlightsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.main.MainActivity;
import com.gmail.jorgegilcavazos.ballislife.features.posts.PostsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.standings.StandingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, DataModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(HighlightsFragment fragment);
    void inject(PostsFragment fragment);
    void inject(AppLocalRepository localRepository);
    void inject(HighlightsRepositoryImpl highlightsRepository);
    void inject(StandingsFragment fragment);
}
