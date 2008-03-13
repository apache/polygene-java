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

import org.qi4j.composite.CompositeBuilder;
import org.qi4j.query.QueryBuilderFactory;

/**
 * All operations on entities goes through an EntitySession. <TODO Muuuch longer explanation needed>
 */
public interface EntitySession
{
    <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType );

    <T extends EntityComposite> T find( String identity, Class<T> compositeType )
        throws EntityCompositeNotFoundException;

    <T extends EntityComposite> T getReference( String identity, Class<T> compositeType )
        throws EntityCompositeNotFoundException;

    void refresh( EntityComposite entity );

    void remove( EntityComposite entity );

    void refresh();

    boolean contains( EntityComposite entity );

    void clear();

    void complete()
        throws SessionCompletionException;

    void discard();

    boolean isOpen();

    QueryBuilderFactory getQueryBuilderFactory();
}
