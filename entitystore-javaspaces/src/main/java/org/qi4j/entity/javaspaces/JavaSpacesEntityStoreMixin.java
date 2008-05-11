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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.qi4j.composite.scope.This;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.service.Activatable;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.structure.Module;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;
import com.j_spaces.core.client.SpaceFinder;

// TODO: add  transaction support

/**
 * Java Spaces implementation of EntityStore. Uses GigaSpaces extensions to Java
 * Spaces (IJSpace and ExternalEntry).
 */
public class JavaSpacesEntityStoreMixin implements EntityStore, Activatable
{
    @This
    JavaSpacesConfiguration config;
    private IJSpace space;

    public org.qi4j.spi.entity.EntityState getEntityState(org.qi4j.spi.structure.CompositeDescriptor compositeDescriptor, QualifiedIdentity identity) throws EntityStoreException
    {

        ExternalEntry template = new ExternalEntry();
        template.setClassName(identity.getCompositeType());
        template.setUID(identity.getIdentity());
        try
        {
            ExternalEntry entry = (ExternalEntry) space.read(template, null, 1000);
            if (entry == null)
            {
                throw new EntityNotFoundException("Space", identity.getIdentity());
            }
            Map<String, Object> map = new HashMap<String, Object>();
            for (int indx = 0; indx < entry.m_FieldsNames.length; indx++)
            {
                map.put(entry.m_FieldsNames[indx], entry.m_FieldsValues[indx]);
            }
            Map<String, Object> properties = new HashMap<String, Object>();
            for (PropertyModel model : compositeDescriptor.getCompositeModel().getPropertyModels())
            {
                String name = model.getQualifiedName();
                properties.put(name, map.get(name));
            }
            Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
            Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
            for (AssociationModel model : compositeDescriptor.getCompositeModel().getAssociationModels())
            {
                String name = model.getName();
                Object value = map.get(name);
                if (value != null)
                {
                    String typeName = ((Class) model.getType()).getName();

                    if (value instanceof String)
                    {
                        String id = (String) value;
                        associations.put(model.getQualifiedName(), new QualifiedIdentity(id, typeName));
                    } else
                    {

                        String[] ids = (String[]) value;
                        Collection<QualifiedIdentity> collection;
                        if (model.getAccessor().getReturnType().equals(ListAssociation.class))
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
                        String qn = model.getQualifiedName();
                        qn = qn.replace('$', '&');
                        manyAssociations.put(qn, collection);
                    }
                }
            }
            return new EntityStateInstance(0, identity, EntityStatus.LOADED, properties, associations, manyAssociations);
        } catch (Exception e)
        {
            throw new EntityNotFoundException("JavaSpaces", identity.getIdentity());
        }
    }

    public org.qi4j.spi.entity.EntityState newEntityState(org.qi4j.spi.structure.CompositeDescriptor arg0, QualifiedIdentity id) throws EntityStoreException
    {
        return new EntityStateInstance(id);
    }

    public StateCommitter prepare(Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates, Module module)
            throws EntityStoreException
    {
        save(newStates);
        save(loadedStates);
        for (QualifiedIdentity id : removedStates)
        {
            ExternalEntry template = new ExternalEntry();
            template.setClassName(id.getCompositeType());
            template.setUID(id.getIdentity());
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
            ExternalEntry template = new ExternalEntry();
            template.setClassName(state.getIdentity().getCompositeType());
            template.setUID(state.getIdentity().getIdentity());
            List<String> names = new ArrayList<String>();
            List<Object> values = new ArrayList<Object>();
            List<String> types = new ArrayList<String>();
            for (String name : state.getPropertyNames())
            {
                names.add(name);
                values.add(state.getProperty(name));
                types.add(String.class.getName());
            }
            for (String association : state.getAssociationNames())
            {
                names.add(association.substring(association.indexOf(':') + 1));
                QualifiedIdentity value = state.getAssociation(association);
                values.add(value.getIdentity());
                types.add(String.class.getName());
            }
            for (String association : state.getManyAssociationNames())
            {
                names.add(association.substring(association.indexOf(':') + 1));
                String[] associations = new String[state.getManyAssociation(association).size()];
                int indx = 0;
                for (QualifiedIdentity id : state.getManyAssociation(association))
                {
                    associations[indx++] = id.getIdentity();
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

    public void activate() throws Exception
    {
        space = (IJSpace) SpaceFinder.find(config.uri().get());
    }

    public void passivate() throws Exception
    {
        space = null;
    }

}
