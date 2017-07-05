package com.gmail.jorgegilcavazos.ballislife.dagger.component;

import com.gmail.jorgegilcavazos.ballislife.dagger.module.AppModule;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.DataModule;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.local.AppLocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesFragment;
import com.gmail.jorgegilcavazos.ballislife.features.highlights.HighlightsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.main.MainActivity;
import com.gmail.jorgegilcavazos.ballislife.features.posts.PostsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.posts.PostsPresenter;
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
    void inject(RedditAuthentication redditAuthentication);
    void inject(PostsRepositoryImpl postsRepository);
    void inject(PostsPresenter postsPresenter);

    void inject(GamesFragment gamesFragment);

    void inject(GamesRepositoryImpl gamesRepository);
}
