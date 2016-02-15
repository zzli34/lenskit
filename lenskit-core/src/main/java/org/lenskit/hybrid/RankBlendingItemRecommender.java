/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.hybrid;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemRecommender;
import org.lenskit.results.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Hybrid item recommender that blends the *ranks* produced by two recommenders.
 *
 * This recommender takes two recommenders, *left* and *right*, and asks them each to produce recommendations.  The
 * final rank for each item is computed by the weighted score of its *rank score*.  If a recommender produces a list
 * $L$ of $n$ recommendations $i_0, i_1, \dots, i_n$, with the rank denoted by $k$, the rank fraction is $1-\frac{k}{n-1}$.
 * That is, the first-ranked item has score 1 and the last 0.
 *
 * The final ranking is done by linearly blending the sub-recommender rank scores using the specified blending weight.
 *
 * This method was devised by Max Harper for use in MovieLens.
 */
public class RankBlendingItemRecommender extends AbstractItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(RankBlendingItemRecommender.class);
    private final ItemRecommender leftRecommender;
    private final ItemRecommender rightRecommender;
    private final double blendWeight;

    /**
     * Construct a new rank-blending recommender.
     * @param left The left recommender.
     * @param right The right recommender.
     * @param w The blending weight.
     */
    @Inject
    public RankBlendingItemRecommender(@Left ItemRecommender left, @Right ItemRecommender right, @BlendWeight double w) {
        leftRecommender = left;
        rightRecommender = right;
        blendWeight = w;
    }

    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        ResultList left = leftRecommender.recommendWithDetails(user, -1, candidates, exclude);
        ResultList right = rightRecommender.recommendWithDetails(user, -1, candidates, exclude);
        logger.debug("recommending for user {} with {} left and {} right recommendations",
                     n, left.size(), right.size());

        return merge(n, left, right, blendWeight);
    }

    static ResultList merge(int n, ResultList left, ResultList right, double weight) {
        Long2DoubleMap leftRanks = computeRankScore(left);
        Long2DoubleMap rightRanks = computeRankScore(right);
        LongSet allItems = new LongOpenHashSet();
        allItems.addAll(leftRanks.keySet());
        allItems.addAll(rightRanks.keySet());

        ScoredItemAccumulator accum = ScoredItemAccumulator.create(n);
        for (LongIterator iter = allItems.iterator(); iter.hasNext();) {
            long item = iter.nextLong();
            double s1 = leftRanks.get(item);
            double s2 = rightRanks.get(item);
            double score = weight * s1 + (1-weight) * s2;
            accum.put(item, score);
        }
        return Results.newResultList(accum.finishResults());
    }

    static Long2DoubleMap computeRankScore(ResultList results) {
        if (results.isEmpty()) {
            return Long2DoubleMaps.EMPTY_MAP;
        } else if (results.size() == 1) {
            return Long2DoubleMaps.singleton(results.get(0).getId(), 1.0);
        }
        final int n = results.size();
        Long2DoubleMap map = new Long2DoubleOpenHashMap(n);
        for (int i = 0; i < n; i++) {
            map.put(results.get(i).getId(), 1.0 - i/(n - 1.0));
        }
        return map;
    }
}
