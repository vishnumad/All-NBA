package com.gmail.jorgegilcavazos.ballislife.data.repository.profile;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import io.reactivex.Single;

/**
 * Provides {@link Contribution}s of a reddit user. Temporarily stores
 * them in memory for fast reuse.
 */
public interface ProfileRepository {

    void setLimit(int limit);

    void setSorting(Sorting sorting);

    void setTimePeriod(TimePeriod timePeriod);

    /**
     * Resets the state of the repository, restarting pagination and clearing any cached
     * contributions if present.
     */
    void reset();

    /**
     * @return an Rx Single that emits the next page of reddit {@link Contribution}s.
     */
    Single<List<Contribution>> next();

    /**
     * @return a list of cached reddit {@link Contribution}s.
     */
    List<Contribution> getCachedContributions();

    /**
     * Removes any saved {@link Contribution}s saved in memory.
     */
    void clearCache();
}
