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
package org.qi4j.entity.neo4j.state;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.qi4j.entity.neo4j.state.direct.DirectEntityState;
import org.qi4j.entity.neo4j.state.direct.DirectIdentityList;
import org.qi4j.entity.neo4j.state.direct.DirectUnorderedCollection;
import org.qi4j.entity.neo4j.state.indirect.IndirectCollection;
import org.qi4j.entity.neo4j.state.indirect.IndirectEntityState;
import org.qi4j.entity.neo4j.state.indirect.IndirectIdentityList;
import org.qi4j.entity.neo4j.state.indirect.IndirectUnorderedCollection;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationModel;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
enum IdentifierCollectionFactory implements DuplicationChecker, BackendFactory
{
    SET( Set.class )
        {
            Collection createDirectCollection( DirectEntityState state, AssociationModel model )
            {
                return new DirectUnorderedCollection( this, state, model );
            }

            IndirectCollection createIndirectCollection( IndirectEntityState state, AssociationModel model )
            {
                return new IndirectUnorderedCollection( this, state.underlyingState.getManyAssociation( model.getQualifiedName() ) );
            }

            public boolean goodToAdd( Iterable<QualifiedIdentity> iterable, QualifiedIdentity qualifiedIdentity )
            {
                for( QualifiedIdentity id : iterable )
                {
                    if( id.equals( qualifiedIdentity ) )
                    {
                        return false;
                    }
                }
                return true;
            }

            public Collection createBackend()
            {
                return new HashSet();
            }
        },

    LIST( List.class )
        {
            Collection createDirectCollection( DirectEntityState state, AssociationModel model )
            {
                return new DirectIdentityList( state, model );
            }

            IndirectCollection createIndirectCollection( IndirectEntityState state, AssociationModel model )
            {
                return new IndirectIdentityList( (List<QualifiedIdentity>) state.underlyingState.getManyAssociation( model.getQualifiedName() ) );
            }

            public boolean goodToAdd( Iterable<QualifiedIdentity> iterable, QualifiedIdentity qualifiedIdentity )
            {
                return true;
            }

            public Collection createBackend()
            {
                throw new IllegalArgumentException( "Cannot create backend for lists." );
            }
        },

    GENERIC( Collection.class )
        {
            Collection createDirectCollection( DirectEntityState state, AssociationModel model )
            {
                return new DirectUnorderedCollection( this, state, model );
            }

            IndirectCollection createIndirectCollection( IndirectEntityState state, AssociationModel model )
            {
                return new IndirectUnorderedCollection( this, state.underlyingState.getManyAssociation( model.getQualifiedName() ) );
            }

            public boolean goodToAdd( Iterable<QualifiedIdentity> iterable, QualifiedIdentity qualifiedIdentity )
            {
                return true;
            }

            public Collection createBackend()
            {
                return new LinkedList();
            }
        };

    private final Class<? extends Collection> type;

    private IdentifierCollectionFactory( Class<? extends Collection> type )
    {
        this.type = type;
    }

    static Collection createCollection( NeoEntityState state, AssociationModel model )
    {
        Class targetType = model.getAccessor().getReturnType();
        for( IdentifierCollectionFactory factory : values() )
        {
            if( factory.type.isAssignableFrom( targetType ) )
            {
                if( state instanceof DirectEntityState )
                {
                    return factory.createDirectCollection( (DirectEntityState) state, model );
                    //} else if (state instanceof IndirectEntityState) {
                    //    return factory.createIndirectCollection((IndirectEntityState)state, model);
                }
                else
                {
                    throw new IllegalArgumentException( "Unknown NeoEntityState implementaion" );
                }
            }
        }
        throw new IllegalArgumentException( "Unknown Association type" );
    }

    abstract Collection createDirectCollection( DirectEntityState state, AssociationModel model );

    abstract IndirectCollection createIndirectCollection( IndirectEntityState state, AssociationModel model );
}
