/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entitystore.neo4j.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.neo4j.api.core.NeoService;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
enum CollectionFactory implements BackendFactory
{
    SET( ManyAssociationType.ManyAssociationTypeEnum.SET, Duplicates.NOT_ALLOWED )
        {
            public <E> Collection<E> createBackend( Class<E> elementType )
            {
                return new HashSet<E>();
            }
        },
    LIST( ManyAssociationType.ManyAssociationTypeEnum.LIST )
        {
            @Override Collection<QualifiedIdentity> createNodeCollection( ManyAssociationFactory factory, DirectEntityState state, NeoService neo, NeoIdentityIndex idIndex )
            {
                return new DirectIdentityList( neo, idIndex, state, factory.getQualifiedName() );
            }

            @Override IndirectCollection createPreloadedCollection( Collection<QualifiedIdentity> manyAssociation )
            {
                return new IndirectIdentityList( (List<QualifiedIdentity>) manyAssociation );
            }

            public <E> Collection<E> createBackend( Class<E> elementType )
            {
                return new ArrayList<E>();
            }
        },
    GENERIC( ManyAssociationType.ManyAssociationTypeEnum.MANY, Duplicates.ALLOWED )
        {
            public <E> Collection<E> createBackend( Class<E> elementType )
            {
                return new LinkedList<E>();
            }
        };

    private static enum Duplicates implements DuplicationChecker
    {
        ALLOWED
            {
                public boolean goodToAdd( Iterable<QualifiedIdentity> iterable, QualifiedIdentity qualifiedIdentity )
                {
                    return true;
                }
            },
        NOT_ALLOWED
            {
                public boolean goodToAdd( Iterable<QualifiedIdentity> iterable, QualifiedIdentity qualifiedIdentity )
                {
                    for( QualifiedIdentity identity : iterable )
                    {
                        if( identity.equals( qualifiedIdentity ) )
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
    }

    private final ManyAssociationType.ManyAssociationTypeEnum type;
    private final DuplicationChecker checker;

    private CollectionFactory( ManyAssociationType.ManyAssociationTypeEnum type )
    {
        this( type, null );
    }

    CollectionFactory( ManyAssociationType.ManyAssociationTypeEnum type, DuplicationChecker checker )
    {
        this.type = type;
        this.checker = checker;
    }

    String typeString()
    {
        return type.name();
    }

    static CollectionFactory getFactoryFor( ManyAssociationType model )
    {
        return getFactoryFor( model.associationType() );
    }

    static CollectionFactory getFactoryFor( String typeString )
    {
        return getFactoryFor( ManyAssociationType.ManyAssociationTypeEnum.valueOf( typeString ) );
    }

    private static CollectionFactory getFactoryFor( ManyAssociationType.ManyAssociationTypeEnum targetType )
    {
        for( CollectionFactory factory : values() )
        {
            if( factory.type == targetType )
            {
                return factory;
            }
        }
        throw new IllegalArgumentException( "Unknown Association type." );
    }

    Collection<QualifiedIdentity> createNodeCollection( ManyAssociationFactory factory, DirectEntityState state, NeoService neo, NeoIdentityIndex idIndex )
    {
        return new DirectUnorderedCollection( idIndex, checker, state, factory.getQualifiedName() );
    }

    IndirectCollection createPreloadedCollection( Collection<QualifiedIdentity> manyAssociation )
    {
        return new IndirectUnorderedCollection( this, manyAssociation );
    }
}
