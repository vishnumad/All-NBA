package com.gmail.jorgegilcavazos.ballislife.dagger.module;

import com.gmail.jorgegilcavazos.ballislife.data.local.AppLocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditServiceImpl;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class BindModule {

    @Binds
    public abstract PostsRepository bindPostsRepository(PostsRepositoryImpl postsRepositoryImpl);

    @Binds
    public abstract RedditService bindRedditService(RedditServiceImpl redditServiceImpl);

    @Binds
    public abstract RedditAuthentication bindRedditAuthentication(
            RedditAuthenticationImpl redditAuthenticationImpl);

    @Binds
    public abstract ProfileRepository bindProfileRepository(
            ProfileRepositoryImpl profileRepositoryImpl);

    @Binds
    public abstract LocalRepository bindLocalRepository(AppLocalRepository appLocalRepository);

    @Binds
    public abstract BaseSchedulerProvider bindBaseSchedulerProvider(
            SchedulerProvider schedulerProvider);

    @Binds
    public abstract GamesRepository bindGamesRepository(GamesRepositoryImpl gamesRepositoryImpl);

    @Binds
    public abstract HighlightsRepository bindHighlightsRepository(
            HighlightsRepositoryImpl highlightsRepositoryImpl);
}
