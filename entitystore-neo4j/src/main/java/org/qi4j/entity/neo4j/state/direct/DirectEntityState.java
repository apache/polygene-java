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
package org.qi4j.entity.neo4j.state.direct;

import java.util.Collection;
import java.util.Iterator;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.qi4j.entity.neo4j.NeoCoreService;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.entity.neo4j.state.LinkType;
import org.qi4j.entity.neo4j.state.NeoEntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectEntityState extends NeoEntityState
{
    final NeoService neo;
    // Construction and Initialization

    DirectEntityState( NeoCoreService neo, NeoIdentityIndex idIndex, Node underlyingNode, QualifiedIdentity identity, EntityStatus status, CompositeDescriptor descriptor )
    {
        super( idIndex, underlyingNode, identity, status, descriptor );
        this.neo = neo;
        if( status == EntityStatus.NEW )
        {
            storeIdentity();
        }
    }

    // NeoEntityState implementation

    public void prepareCommit()
    {
        incNodeVersion();
    }

    protected void storeProperty( String qualifiedName, Object value )
    {
        underlyingNode.setProperty( qualifiedName, value );
    }

    // EntityState implementation

    public void remove()
    {
        for( Relationship relation : underlyingNode.getRelationships( Direction.INCOMING ) )
        {
            if( !LinkType.END.isInstance( relation ) )
            {
                throw new IllegalStateException( "Cannot remove entity with identity: " + identity
                                                 + ". It has incoming associtaions." );
            }
        }
        for( String qName : manyAssociationsModels.keySet() )
        {
            for( Iterator iter = getManyAssociation( qName ).iterator(); iter.hasNext(); )
            {
                iter.next();
                iter.remove();
            }
        }
        for( Relationship relation : underlyingNode.getRelationships( Direction.OUTGOING ) )
        {
            relation.delete();
        }
        underlyingNode.delete();
    }

    public long getEntityVersion()
    {
        return getNodeVersion();
    }

    public Object getProperty( String qualifiedName )
    {
        return underlyingNode.getProperty( qualifiedName, null );
    }

    public QualifiedIdentity getAssociation( String qualifiedName )
    {
        return getSingleAssociation( qualifiedName );
    }

    public void setAssociation( String qualifiedName, QualifiedIdentity newEntity )
    {
        createAssociation( qualifiedName, newEntity, false );
    }

    public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
    {
        return createManyAssociation( qualifiedName );
    }

}
