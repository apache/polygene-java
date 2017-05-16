/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.spi.entity;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.entity.EntityReference;

/**
 * State holder for NamedAssociations.
 * The actual state can be eager-loaded or lazy-loaded.
 * This is an implementation detail.
 * The Iterable&lt;String&gt; returns the names in the association set.
 */
public interface NamedAssociationState
    extends Iterable<String>
{
    int count();

    boolean containsName( String name );

    boolean put( String name, EntityReference entityReference );

    boolean remove( String name );

    boolean clear();

    EntityReference get( String name );

    String nameOf( EntityReference entityReference );

    default Stream<Map.Entry<String, EntityReference>> stream()
    {
        return StreamSupport.stream( spliterator(), false )
                            .map( name -> new AbstractMap.SimpleImmutableEntry<>( name, get( name ) ) );
    }
}
