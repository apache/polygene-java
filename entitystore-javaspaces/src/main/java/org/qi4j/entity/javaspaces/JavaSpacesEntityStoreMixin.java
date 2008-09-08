/*  Copyright 2008 Jan Kronquist.
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
package org.qi4j.entity.javaspaces;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;
import com.j_spaces.core.client.SpaceFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.jini.core.entry.Entry;
import org.qi4j.injection.scope.This;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;

// TODO: add  transaction support

/**
 * Java Spaces implementation of EntityStore. Uses GigaSpaces extensions to Java
 * Spaces (IJSpace and ExternalEntry).
 */
public class JavaSpacesEntityStoreMixin
    extends EntityTypeRegistryMixin
    implements EntityStore, Activatable
{
    @This Configuration<JavaSpacesConfiguration> config;
    private IJSpace space;

    public EntityState getEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException, EntityNotFoundException
    {
        EntityType entityType = getEntityType( anIdentity.type() );

        ExternalEntry template = new ExternalEntry();
        template.setUID(anIdentity.toString());
        try
        {
            ExternalEntry entry = (ExternalEntry) space.read(template, null, 1000);
            if (entry == null)
            {
                throw new EntityNotFoundException("Space", anIdentity);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            for (int indx = 0; indx < entry.m_FieldsNames.length; indx++)
            {
                map.put(entry.m_FieldsNames[indx], entry.m_FieldsValues[indx]);
            }
            Map<String, Object> properties = new HashMap<String, Object>();
            for ( PropertyType propertyType : entityType.properties())
            {
                String name = propertyType.qualifiedName();
                properties.put(name, map.get(name));
            }
            Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
            Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
            for ( AssociationType associationType : entityType.associations())
            {
                String name = associationType.qualifiedName();
                Object value = map.get(name);
                if (value != null)
                {
                    String typeName = associationType.type();

                    String id = (String) value;
                    associations.put(associationType.qualifiedName(), new QualifiedIdentity(id, typeName));
                }
            }
            
            for ( ManyAssociationType manyAssociationType : entityType.manyAssociations())
            {
                String name = manyAssociationType.qualifiedName();
                Object value = map.get(name);
                if (value != null)
                {
                    String typeName = manyAssociationType.type();

                    if (value instanceof String)
                    {
                        String id = (String) value;
                        associations.put(name, new QualifiedIdentity(id, typeName));
                    } else
                    {

                        String[] ids = (String[]) value;
                        Collection<QualifiedIdentity> collection;
                        if (manyAssociationType.associationType() == ManyAssociationType.ManyAssociationTypeEnum.LIST)
                        {
                            collection = new ArrayList<QualifiedIdentity>();
                        } else
                        {
                            collection = new HashSet<QualifiedIdentity>();
                        }
                        for (int indx = 0; indx < ids.length; indx++)
                        {
                            collection.add(new QualifiedIdentity(ids[indx], typeName));
                        }
                        String qn = name;
                        qn = qn.replace('$', '&');
                        manyAssociations.put(qn, collection);
                    }
                }
            }
            return new DefaultEntityState(0, System.currentTimeMillis(), anIdentity, EntityStatus.LOADED, entityType,
                                          properties, associations, manyAssociations);
        } catch (Exception e)
        {
            throw new EntityNotFoundException("JavaSpaces", anIdentity);
        }
    }

    public EntityState newEntityState(QualifiedIdentity id) throws EntityStoreException
    {
        return new DefaultEntityState(id, getEntityType( id.type() ));
    }

    public StateCommitter prepare(Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates)
            throws EntityStoreException
    {
        save(newStates);
        save(loadedStates);
        for (QualifiedIdentity id : removedStates)
        {
            ExternalEntry template = new ExternalEntry();
            template.setUID(id.toString());
            try
            {
                space.take(template, null, 0);
            } catch (Exception e)
            {
                throw new EntityStoreException(e);
            }
        }
        return new StateCommitter()
        {
            public void cancel()
            {
            }

            public void commit()
            {
            }
        };
    }

    private void save(Iterable<EntityState> newStates)
    {
        for (EntityState state : newStates)
        {
            EntityType entityType =  state.entityType();
            ExternalEntry template = new ExternalEntry();
            template.setUID(state.qualifiedIdentity().toString());
            String type = entityType.type();
            type = type.substring( type.lastIndexOf( '.' )+1);
            template.setClassName(type );
            List<String> names = new ArrayList<String>();
            List<Object> values = new ArrayList<Object>();
            List<String> types = new ArrayList<String>();
            for( PropertyType propertyType : entityType.properties() )
            {
                String name = propertyType.qualifiedName();
                names.add(name);
                values.add(state.getProperty(name));
                types.add(propertyType.type());
            }
            for( AssociationType associationType : entityType.associations() )
            {
                String association = associationType.qualifiedName();
                names.add(association);
                QualifiedIdentity value = state.getAssociation(association);
                if (value != null)
                    values.add(value.toString());
                else
                    values.add(null);
                types.add(String.class.getName());
            }
            for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
            {
                String association = manyAssociationType.qualifiedName();
                names.add(association);
                String[] associations = new String[state.getManyAssociation(association).size()];
                int indx = 0;
                for (QualifiedIdentity id : state.getManyAssociation(association))
                {
                    associations[indx++] = id.toString();
                }
                values.add(associations);
                types.add(String[].class.getName());
            }
            template.m_FieldsTypes = types.toArray(new String[types.size()]);
            template.m_FieldsNames = names.toArray(new String[names.size()]);
            template.m_FieldsValues = values.toArray(new Object[values.size()]);
            try
            {
                space.write(template, null, net.jini.core.lease.Lease.FOREVER);
            } catch (Exception e)
            {
                throw new EntityStoreException(e);
            }
        }
    }

    public Iterator<EntityState> iterator()
    {
        ExternalEntry template = new ExternalEntry();
        template.setClassName( Object.class.getName() );

        try
        {
            // TODO Iterate here to get all the items
            Entry[] entries = space.readMultiple( template, null, 1000 );
            List<EntityState> states = new ArrayList<EntityState>(entries.length);
            for( int i = 0; i < entries.length; i++ )
            {
                ExternalEntry entry = (ExternalEntry)entries[ i ];
                QualifiedIdentity qid = QualifiedIdentity.parseQualifiedIdentity( entry.getUID());
                EntityState entityState = getEntityState( qid );
                states.add( entityState );
            }
            return states.iterator();
        }
        catch( Exception e )
        {
            throw new EntityStoreException(e);
        }
    }

    public void activate() throws Exception
    {
        space = (IJSpace) SpaceFinder.find(config.configuration().uri().get());
    }

    public void passivate() throws Exception
    {
        space = null;
    }


}
