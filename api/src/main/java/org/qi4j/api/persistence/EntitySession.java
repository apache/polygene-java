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
package org.qi4j.api.persistence;

import java.net.URL;
import org.qi4j.api.CompositeBuilder;

public interface EntitySession
{
    <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType );

    <T> T attach( T entity );

    void remove( EntityComposite entity );

    <T extends EntityComposite> T find( String identity, Class<T> compositeType );

    <T extends EntityComposite> T getReference( String identity, Class<T> compositeType );

    void refresh( EntityComposite entity );

    void clear();

    boolean contains( EntityComposite entity );

    void close();

    boolean isOpen();

    /**
     * Create a URL for the composite of the given identity.
     *
     * @param identity The identity of the object to convert into a URL.
     * @return The URL to the composite of the given identity.
     */
    URL toURL( Identity identity );

    QueryFactory getQueryFactory();
}
