/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entity;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.query.QueryBuilderFactory;

/**
 * All operations on entities goes through an UnitOfWork. <TODO Muuuch longer explanation needed>
 */
public interface UnitOfWork
{
    UnitOfWork newUnitOfWork();

    <T> T newEntity( Class<T> type )
        throws NoSuchEntityException, LifecycleException;

    <T> T newEntity( String identity, Class<T> type )
        throws NoSuchEntityException, LifecycleException;

    <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws NoSuchEntityException;

    <T> EntityBuilder<T> newEntityBuilder( String identity, Class<T> type )
        throws NoSuchEntityException;

    <T> T find( String identity, Class<T> type )
        throws EntityCompositeNotFoundException;

    <T> T getReference( String identity, Class<T> type )
        throws EntityCompositeNotFoundException;

    <T> T dereference( T entity )
        throws EntityCompositeNotFoundException;

    void refresh( Object entity )
        throws UnitOfWorkException;

    void refresh();

    boolean contains( Object entity );

    void reset();

    void remove( Object entity )
        throws LifecycleException;

    void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException;

    void discard();

    boolean isOpen();

    /**
     * Pauses this UnitOfWork.
     * <p>
     * Calling this method will cause the underlying UnitOfWork to become the current UnitOfWork until the
     * the resume() method is called. It is the client's responsibility not to drop the reference to this
     * UnitOfWork while being paused.
     * </p>
     */
    void pause();

    /**
     * Resumes this UnitOfWork to again become the current UnitOfWork.
     */
    void resume();

    QueryBuilderFactory queryBuilderFactory();

    CompositeBuilderFactory compositeBuilderFactory();

    ObjectBuilderFactory objectBuilderFactory();

    void registerUnitOfWorkCallback( UnitOfWorkCallback callback );

    LoadingPolicy loadingPolicy();

    void setLoadingPolicy( LoadingPolicy loadingPolicy );
}
