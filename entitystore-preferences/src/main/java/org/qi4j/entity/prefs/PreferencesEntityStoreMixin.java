/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entity.prefs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.qi4j.entity.association.GenericAssociationInfo;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.structure.Application;
import org.qi4j.util.MetaInfo;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * @see Preferences
 */
public class PreferencesEntityStoreMixin
    extends EntityTypeRegistryMixin
    implements Activatable
{
    private @Uses ServiceDescriptor descriptor;
    private @Structure Application application;

    private Preferences root;

    public void activate()
        throws Exception
    {
        root = getApplicationRoot();
    }

    private Preferences getApplicationRoot()
    {
        MetaInfo metaInfo = descriptor.metaInfo();
        PreferenceEntityStoreInfo storeInfo = metaInfo.get( PreferenceEntityStoreInfo.class );

        Preferences preferences;
        if( storeInfo == null )
        {
            // Default to use system root
            preferences = Preferences.systemRoot();
        }
        else
        {
            PreferenceEntityStoreInfo.PreferenceNode rootNode = storeInfo.getRootNode();
            preferences = rootNode.getNode();
        }

        String name = application.name();
        return preferences.node( name );
    }

    public void passivate()
        throws Exception
    {
    }

    public EntityState newEntityState( QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        return new DefaultEntityState( anIdentity, getEntityType( anIdentity.type() ) );
    }

    public EntityState getEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
        EntityType entityType = getEntityType( identity.type() );
        Map<String, Collection<QualifiedIdentity>> manyAssociations = DefaultEntityState.newManyCollections( entityType );

        try
        {
            if( !root.nodeExists( identity.identity() ) )
            {
                throw new EntityNotFoundException( descriptor.identity(), identity );
            }
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
        Preferences preferences = getNode( identity );
        long version = preferences.getLong( "qi4j-version", 0L );
        loadProperties( preferences, properties, entityType );
        loadAssociations( preferences, associations, entityType );
        loadManyAssociations( preferences, manyAssociations, entityType );
        DefaultEntityState entityState = new DefaultEntityState(
            version, System.currentTimeMillis(), identity, EntityStatus.LOADED, entityType,
            properties, associations, manyAssociations );

        return entityState;
    }

    private Preferences getNode( QualifiedIdentity anIdentity )
    {
        return root.node( anIdentity.identity() );
    }

    private void loadProperties( Preferences preferences, Map<String, Object> properties, EntityType type )
        throws EntityStoreException
    {
        try
        {
            for( PropertyType propertyType : type.properties() )
            {
                String name = GenericPropertyInfo.getName( propertyType.qualifiedName() );
                Object value = null;
                if( propertyType.type().equals( String.class.getName() ) )
                {
                    value = preferences.get( name, null );
                }
                else if( propertyType.type().equals( Long.class.getName() ) )
                {
                    value = preferences.getLong( name, 0L );
                }
                else if( propertyType.type().equals( Integer.class.getName() ) )
                {
                    value = preferences.getInt( name, 0 );
                }
                else if( propertyType.type().equals( Boolean.class.getName() ) )
                {
                    value = preferences.getBoolean( name, false );
                }
                else if( propertyType.type().equals( Float.class.getName() ) )
                {
                    value = preferences.getFloat( name, 0F );
                }
                else if( propertyType.type().equals( Double.class.getName() ) )
                {
                    value = preferences.getDouble( name, 0D );
                }
                else
                {
                    byte[] bytes = preferences.getByteArray( name, null );
                    if( bytes != null )
                    {
                        ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                        ObjectInputStream oin = new ObjectInputStream( bin );
                        value = oin.readObject();
                    }
                }
                properties.put( propertyType.qualifiedName(), value );
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    private void loadAssociations(
        Preferences preferences,
        Map<String, QualifiedIdentity> associations,
        EntityType type )
        throws EntityStoreException
    {
        try
        {
            for( AssociationType associationType : type.associations() )
            {
                String name = GenericAssociationInfo.getName( associationType.qualifiedName() );

                String value = preferences.get( name, null );

                if( value != null )
                {
                    associations.put( associationType.qualifiedName(), QualifiedIdentity.parseQualifiedIdentity( value ) );
                }
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    private void loadManyAssociations(
        Preferences preferences,
        Map<String, Collection<QualifiedIdentity>> associations,
        EntityType type )
        throws EntityStoreException
    {
        try
        {
            for( ManyAssociationType manyAssociationType : type.manyAssociations() )
            {
                String name = GenericAssociationInfo.getName( manyAssociationType.qualifiedName() );
                Collection<QualifiedIdentity> collection = associations.get( manyAssociationType.qualifiedName() );
                String value = preferences.get( name, null );

                if( value != null )
                {
                    BufferedReader reader = new BufferedReader( new StringReader( value ) );
                    String qidString;
                    while( ( qidString = reader.readLine() ) != null )
                    {
                        QualifiedIdentity qid = QualifiedIdentity.parseQualifiedIdentity( qidString );
                        collection.add( qid );
                    }
                }
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    public StateCommitter prepare(
        Iterable<EntityState> newStates,
        Iterable<EntityState> updatedStates,
        Iterable<QualifiedIdentity> removedStates )
        throws EntityStoreException
    {
        for( EntityState newState : newStates )
        {
            DefaultEntityState state = (DefaultEntityState) newState;
            Preferences node = getNode( state.qualifiedIdentity() );
            copyStateToNode( state, node );
            node.put( "type", state.qualifiedIdentity().type() );
        }

        for( EntityState updatedState : updatedStates )
        {
            DefaultEntityState state = (DefaultEntityState) updatedState;
            Preferences node = getNode( state.qualifiedIdentity() );
            copyStateToNode( state, node );
            if( state.isModified() )
            {
                node.putLong( "qi4j-version", state.version() + 1 );
            }
        }

        for( QualifiedIdentity removedState : removedStates )
        {
            try
            {
                getNode( removedState ).removeNode();
            }
            catch( BackingStoreException e )
            {
                throw new EntityStoreException( e );
            }
        }

        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    root.flush();
                }
                catch( BackingStoreException e )
                {
                    throw new EntityStoreException( e );
                }
            }

            public void cancel()
            {
                try
                {
                    root.sync();
                }
                catch( BackingStoreException e )
                {
                    // Ignore
                    root = getApplicationRoot();
                }
            }
        };
    }

    private void copyStateToNode( DefaultEntityState state, Preferences node )
    {
        // Properties
        for( PropertyType property : state.entityType().properties() )
        {
            Object value = state.getProperty( property.qualifiedName() );
            String propertyName = GenericPropertyInfo.getName( property.qualifiedName() );
            if( value == null )
            {
                node.remove( propertyName );
            }
            else if( value instanceof String )
            {
                node.put( propertyName, (String) value );
            }
            else if( value instanceof Long )
            {
                node.putLong( propertyName, (Long) value );
            }
            else if( value instanceof Integer )
            {
                node.putInt( propertyName, (Integer) value );
            }
            else if( value instanceof Boolean )
            {
                node.putBoolean( propertyName, (Boolean) value );
            }
            else if( value instanceof Float )
            {
                node.putFloat( propertyName, (Float) value );
            }
            else if( value instanceof Double )
            {
                node.putDouble( propertyName, (Double) value );
            }
            else
            {
                try
                {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream( bout );
                    oout.writeObject( value );
                    oout.close();
                    node.putByteArray( propertyName, bout.toByteArray() );
                }
                catch( IOException e )
                {
                    throw new EntityStoreException( e );
                }
            }
        }

        // Associations
        for( AssociationType associationType : state.entityType().associations() )
        {
            QualifiedIdentity qid = state.getAssociation( associationType.qualifiedName() );
            String associationName = GenericAssociationInfo.getName( associationType.qualifiedName() );
            if( qid == null )
            {
                node.remove( associationName );
            }
            else
            {
                node.put( associationName, qid.toString() );
            }
        }

        // ManyAssociations
        for( ManyAssociationType manyAssociationType : state.entityType().manyAssociations() )
        {
            String associationName = GenericAssociationInfo.getName( manyAssociationType.qualifiedName() );
            Collection<QualifiedIdentity> qids = state.getManyAssociation( manyAssociationType.qualifiedName() );
            StringBuilder qidsString = new StringBuilder();
            for( QualifiedIdentity qid : qids )
            {
                if( !( qidsString.length() == 0 ) )
                {
                    qidsString.append( '\n' );
                }
                qidsString.append( qid.toString() );
            }
            node.put( associationName, qidsString.toString() );
        }
    }

    public Iterator<EntityState> iterator()
    {
        try
        {
            List<EntityState> states = new ArrayList<EntityState>();
            addStates( states, root );
            return states.iterator();
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private void addStates( List<EntityState> states, Preferences root )
        throws BackingStoreException
    {
        String identity = root.get( "identity", null );
        if( identity != null )
        {
            QualifiedIdentity qid = new QualifiedIdentity( identity, root.get( "type", null ) );
            EntityState state = getEntityState( qid );
            states.add( state );
        }

        // Add children
        for( String nodeName : root.childrenNames() )
        {
            Preferences node = root.node( nodeName );
            addStates( states, node );
        }
    }
}
