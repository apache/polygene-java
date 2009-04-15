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

package org.qi4j.entitystore.prefs;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.DefaultLazyEntityState;
import org.qi4j.spi.entity.helpers.DefaultManyAssociationState;
import org.qi4j.spi.entity.helpers.LazyStateLoader;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * @see Preferences
 */
public class PreferencesEntityStoreMixin
        implements EntityStore, Activatable, LazyStateLoader
{
    private
    @Uses
    ServiceDescriptor descriptor;
    private
    @Structure
    Application application;

    private Preferences root;

    public void activate()
            throws Exception
    {
        root = getApplicationRoot();
    }

    private Preferences getApplicationRoot()
    {
        MetaInfo metaInfo = descriptor.metaInfo();
        PreferenceEntityStoreInfo storeInfo = metaInfo.get(PreferenceEntityStoreInfo.class);

        Preferences preferences;
        if (storeInfo == null)
        {
            // Default to use system root
            preferences = Preferences.systemRoot();
        } else
        {
            PreferenceEntityStoreInfo.PreferenceNode rootNode = storeInfo.getRootNode();
            preferences = rootNode.getNode();
        }

        String name = application.name();
        return preferences.node(name);
    }

    public void passivate()
            throws Exception
    {
    }

    public EntityState newEntityState(EntityReference anReference)
            throws EntityStoreException
    {
        return new DefaultEntityState(anReference);
    }

    public EntityState getEntityState(EntityReference reference)
            throws EntityStoreException
    {
        Map<StateName, String> properties = new HashMap<StateName, String>();
        Map<StateName, EntityReference> associations = new HashMap<StateName, EntityReference>();
        Map<StateName, ManyAssociationState> manyAssociations = new HashMap<StateName, ManyAssociationState>();

        try
        {
            if (!root.nodeExists(reference.identity()))
            {
                throw new EntityNotFoundException(reference);
            }
        }
        catch (BackingStoreException e)
        {
            throw new EntityStoreException(e);
        }
        Preferences preferences = getNode(reference);
        long version = preferences.getLong("version", 0L);

        return new DefaultLazyEntityState(
                version,
                System.currentTimeMillis(),
                reference,
                EntityStatus.LOADED,
                Collections.<EntityTypeReference>emptySet(),
                properties,
                associations,
                manyAssociations,
                this);
    }

    private Preferences getNode(EntityReference anReference)
    {
        return root.node(anReference.identity());
    }

    public String getProperty(EntityReference reference, StateName stateName)
            throws EntityStoreException
    {
        Preferences preferences = getNode(reference);
        String name = stateName.qualifiedName().name();
        return preferences.get(name, null);
    }

    public EntityReference getAssociation(EntityReference reference, StateName stateName)
    {
        try
        {
            Preferences preferences = getNode(reference);
            String name = stateName.qualifiedName().name();

            String value = preferences.get(name, null);

            EntityReference ref = null;
            if (value != null)
            {
                ref = EntityReference.parseEntityReference(value);
            }

            return ref;
        }
        catch (Exception e)
        {
            throw new EntityStoreException(e);
        }
    }

    public ManyAssociationState getManyAssociation(EntityReference reference, StateName stateName)
    {
        try
        {
            Preferences preferences = getNode(reference);
            String name = stateName.qualifiedName().name();
            ManyAssociationState state = new DefaultManyAssociationState();
            String value = preferences.get(name, null);

            if (value != null)
            {
                BufferedReader reader = new BufferedReader(new StringReader(value));
                String qidString;
                while ((qidString = reader.readLine()) != null)
                {
                    EntityReference qid = EntityReference.parseEntityReference(qidString);
                    state.add(state.count(), qid);
                }
            }

            return state;
        }
        catch (Exception e)
        {
            throw new EntityStoreException(e);
        }
    }

    public StateCommitter prepare(
            Iterable<EntityState> newStates,
            Iterable<EntityState> updatedStates,
            Iterable<EntityReference> removedStates)
            throws EntityStoreException
    {
        for (EntityState newState : newStates)
        {
            DefaultEntityState state = (DefaultEntityState) newState;
            Preferences node = getNode(state.identity());
            copyStateToNode(state, node);
        }

        for (EntityState updatedState : updatedStates)
        {
            DefaultEntityState state = (DefaultEntityState) updatedState;
            Preferences node = getNode(state.identity());
            copyStateToNode(state, node);
            if (state.isModified())
            {
                node.putLong("version", state.version() + 1);
            }
        }

        for (EntityReference removedState : removedStates)
        {
            try
            {
                getNode(removedState).removeNode();
            }
            catch (BackingStoreException e)
            {
                throw new EntityStoreException(e);
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
                catch (BackingStoreException e)
                {
                    throw new EntityStoreException(e);
                }
            }

            public void cancel()
            {
                try
                {
                    root.sync();
                }
                catch (BackingStoreException e)
                {
                    // Ignore
                    root = getApplicationRoot();
                }
            }
        };
    }

    private void copyStateToNode(DefaultEntityState state, Preferences node)
    {
        // Properties
        for (Map.Entry<StateName, String> stateNameObjectEntry : state.getProperties().entrySet())
        {
            Object value = stateNameObjectEntry.getValue();
            String propertyName = stateNameObjectEntry.getKey().qualifiedName().name();
            if (value == null)
            {
                node.remove(propertyName);
            } else
            {
                node.put(propertyName, (String) value);
            }
        }

        // Associations
        for (Map.Entry<StateName, EntityReference> stateNameEntityReferenceEntry : state.getAssociations().entrySet())
        {
            EntityReference reference = stateNameEntityReferenceEntry.getValue();
            String associationName = stateNameEntityReferenceEntry.getKey().qualifiedName().name();
            if (reference == null)
            {
                node.remove(associationName);
            } else
            {
                node.put(associationName, reference.toString());
            }
        }

        // ManyAssociations
        for (Map.Entry<StateName, ManyAssociationState> stateNameManyAssociationStateEntry : state.getManyAssociations().entrySet())
        {

            String associationName = stateNameManyAssociationStateEntry.getKey().qualifiedName().name();
            ManyAssociationState qids = stateNameManyAssociationStateEntry.getValue();
            StringBuilder qidsString = new StringBuilder();
            for (EntityReference qid : qids)
            {
                if (!(qidsString.length() == 0))
                {
                    qidsString.append('\n');
                }
                qidsString.append(qid.toString());
            }
            node.put(associationName, qidsString.toString());
        }
    }

    public void visitEntityStates(EntityStateVisitor visitor)
    {
        try
        {
            List<EntityState> states = new ArrayList<EntityState>();
            addStates(states, root);
            for (EntityState entityState : states)
            {
                visitor.visitEntityState(entityState);
            }
        }
        catch (BackingStoreException e)
        {
            throw new EntityStoreException(e);
        }
    }

    private void addStates(List<EntityState> states, Preferences root)
            throws BackingStoreException
    {
        String identity = root.get("identity", null);
        if (identity != null)
        {
            EntityReference qid = new EntityReference(identity);
            EntityState state = getEntityState(qid);
            states.add(state);
        }

        // Add children
        for (String nodeName : root.childrenNames())
        {
            Preferences node = root.node(nodeName);
            addStates(states, node);
        }
    }
}
