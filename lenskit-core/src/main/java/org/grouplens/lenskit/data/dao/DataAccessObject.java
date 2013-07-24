/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.dao;

import java.io.Closeable;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.LongCursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;

/**
 * LensKit core data access object interface. This interface provides access to
 * users, items, and event histories to LensKit recommenders.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface DataAccessObject extends Closeable {
    /**
     * Retrieve the users from the data source.
     *
     * @return a cursor iterating the user IDs.
     */
    LongCursor getUsers();

    /**
     * Get the number of users in the system. This should be the same number of
     * users that will be returned by iterating {@link #getUsers()} (unless a
     * user is added or removed between the two calls).
     *
     * @return The number of users in the system.
     */
    int getUserCount();

    /**
     * Retrieve the items from the data source.
     *
     * @return a cursor iterating the item IDs.
     */
    LongCursor getItems();

    /**
     * Get the number of items in the system. This should be the same number of
     * items that will be returned by iterating {@link #getItems()} (unless an
     * item is added or removed between the two calls).
     *
     * @return The number of items in the system.
     */
    int getItemCount();

    /**
     * Get all events from the data set. Equivalent to
     * {@code getEvents(Event.class, SortOrder.ANY)}.
     *
     * @return A cursor iterating over all events.
     * @see #getEvents(Class, SortOrder)
     */
    Cursor<? extends Event> getEvents();

    /**
     * Get all events with a specified sort order. Equivalent to
     * {@code getEvents(Event.class, order)}.
     *
     * @param order The sort to apply for the ratings.
     * @return The events in order.
     * @throws UnsupportedQueryException if the sort order is not supported by
     *                                   this data source.
     * @see #getEvents(Class, SortOrder)
     */
    Cursor<? extends Event> getEvents(SortOrder order) throws UnsupportedQueryException;

    /**
     * Get all events of a particular type. Equivalent to
     * {@code getEvents(type, SortOrder.ANY)}.
     *
     * @param <E>  The type of event.
     * @param type The type of event to retrieve. All events of this type,
     *             including subclasses, are returned.
     * @return The events of type {@var type}.
     * @see #getEvents(Class, SortOrder)
     */
    <E extends Event> Cursor<E> getEvents(Class<E> type);

    /**
     * Get all events of a particular type with a specified sort order.
     *
     * @param <E>   The type of event.
     * @param type  The type of event to retrieve. All events of this type,
     *              including subclasses, are returned.
     * @param order The sort to apply for the ratings.
     * @return The events of type {@var type} in order.
     * @throws UnsupportedQueryException if the sort order is not supported by
     *                                   this data source.
     */
    <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) throws UnsupportedQueryException;

    /**
     * Get the user history for a user.
     *
     * @param user The user whose history is needed.
     * @return The history of {@code user}.
     */
    UserHistory<Event> getUserHistory(long user);

    /**
     * Get the user history for a user filtered by type.
     *
     * @param <E>  The type of event.
     * @param user The user whose history is needed.
     * @param type The type of events to retrieve.
     * @return The history of {@code user}, filtered to only contain events of the specified
     *         type.
     */
    <E extends Event> UserHistory<E> getUserHistory(long user, Class<E> type);

    /**
     * Get all user event histories from the system. This serves as a
     * {@link #getEvents()} call grouped by user.
     *
     * @return A cursor returning the event history for each user in the data
     *         source.
     * @see #getUserHistories(Class)
     */
    Cursor<UserHistory<Event>> getUserHistories();

    /**
     * Get all user event histories from the system, filtering events by type.
     * This serves as a {@link #getEvents(Class)} call grouped by user.
     *
     * @param <E>  The type of event.
     * @param type The type of event to retrieve.
     * @return A cursor iterating the event history for each user.
     */
    <E extends Event> Cursor<UserHistory<E>> getUserHistories(Class<E> type);

    /**
     * Get all events for the specified user.
     *
     * @param userId The ID of the user whose events are requested.
     * @return An iterator over the user's events in timestamp order.
     * @see #getUserEvents(long, Class)
     */
    Cursor<? extends Event> getUserEvents(long userId);

    /**
     * Get events of a particular type for the specified user.
     *
     * @param <E>    The type of event.
     * @param userId The ID of the user whose ratings are requested.
     * @param type   The type of event to retrieve.
     * @return An iterator over the user's events in timestamp order.
     */
    <E extends Event> Cursor<E> getUserEvents(long userId, Class<E> type);

    /**
     * Get all events related to the specified item.
     *
     * @param itemId The ID of the item whose events are requested. The events
     *               are first sorted by user, then by timestamp.
     * @return The events involving the specified item.
     * @see #getItemEvents(long, Class)
     */
    Cursor<? extends Event> getItemEvents(long itemId);

    /**
     * Get all ratings for the specified item.
     *
     * @param <E>    The type of event.
     * @param itemId The ID of the item whose events are requested.
     * @param type   The type of events to retrieve.
     * @return An iterator over the item's events. The events are first sorted
     *         by user, then by timestamp.
     */
    <E extends Event> Cursor<E> getItemEvents(long itemId, Class<E> type);

    /**
     * Close this DAO so that any underlying data session is closed. The DAO is
     * no longer usable, and a new DAO must be re-opened from a
     * DataAccessObjectManager.
     */
    @Override
    void close();
}