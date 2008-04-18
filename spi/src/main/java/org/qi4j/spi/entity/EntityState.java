/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entity;

import java.util.Collection;

/**
 * The EntityState is a shared "second-level cache" mixin state container for a particular
 * entity. All mixins are shared across transactions, and therefor there is a "copy-on-write" pattern
 * expected from the higher-level systems. This means that when a UnitOfWork in a transaction wants
 * to modify the mixin state, it must first retrieve a non-shared copy from the EntityState by
 * calling <code><K> K getMixinResolution( Class<K> mixinType );</code>. For instance;
 * <code><pre>
 *     CompositeState cs = ...;
 *     Map<Class, Object> mixins = cs.getMixins();
 *     Name name = entityHolder.getMixinResolution( Name.class );
 *     name.setName( someName ); // initiated by client code.
 *     mixins.put( Name.class, name );
 * </pre></code>
 */
public interface EntityState
{
    /**
     * Returns the identity of the entity that this EntityState represents.
     *
     * @return the identity of the entity that this EntityState represents.
     */
    QualifiedIdentity getIdentity();

    long getEntityVersion();

    /**
     * Remove the entity represented by this EntityState when the unit of work is completed.
     */
    void remove();

    EntityStatus getStatus();

    Object getProperty( String qualifiedName );

    void setProperty( String qualifiedName, Object newValue );

    QualifiedIdentity getAssociation( String qualifiedName );

    void setAssociation( String qualifiedName, QualifiedIdentity newEntity );

    Collection<QualifiedIdentity> getManyAssociation( String qualifiedName );

    Collection<QualifiedIdentity> setManyAssociation( String qualifiedName, Collection<QualifiedIdentity> newManyAssociation );

    Iterable<String> getPropertyNames();

    Iterable<String> getAssociationNames();

    Iterable<String> getManyAssociationNames();
}
