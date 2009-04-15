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

import org.neo4j.api.core.Node;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class LoadedDescriptor
{
    private static final Map<Node, LoadedDescriptor> cache = new ConcurrentHashMap<Node, LoadedDescriptor>();
    private static final String FACTORY_TYPE_PROPERTY_PREFIX = "factoryTypeFor::";
    private static final String ASSOCIATIONS_PROPERTY_KEY = "<qualified association names>";
    private static final String PROPERTIES_PROPERTY_KEY = "<qualified property names>";
    private static final String MANY_ASSOCIATIONS_PROPERTY_KEY = "<qualified many association names>";

    public static LoadedDescriptor loadDescriptor(EntityType entityType, Node descriptionNode)
    {
        LoadedDescriptor result = cache.get(descriptionNode);
        if (result == null)
        {
            synchronized (cache)
            {
                result = cache.get(descriptionNode);
                if (result == null)
                {
                    result = new LoadedDescriptor(entityType, descriptionNode);
                    cache.put(descriptionNode, result);
                }
            }
        }
        result.verify(entityType);
        return result;
    }

    public static LoadedDescriptor loadDescriptor(Node descriptionNode)
    {
        LoadedDescriptor result = cache.get(descriptionNode);
        if (result == null)
        {
            synchronized (cache)
            {
                result = cache.get(descriptionNode);
                if (result == null)
                {
                    result = new LoadedDescriptor(descriptionNode);
                    cache.put(descriptionNode, result);
                }
            }
        }
        return result;
    }

    private final Node descriptionNode;
    private final List<ManyAssociationFactory> manyAssociations = new LinkedList<ManyAssociationFactory>();
    private final List<StateName> associations = new LinkedList<StateName>();
    private final List<StateName> properties = new LinkedList<StateName>();

    private LoadedDescriptor(EntityType descriptor, Node descriptionNode)
    {
        this.descriptionNode = descriptionNode;
        for (AssociationType model : descriptor.associations())
        {
            associations.add(model.stateName());
        }
        for (ManyAssociationType model : descriptor.manyAssociations())
        {
            manyAssociations.add(ManyAssociationFactory.getFactory(model));
        }
        for (PropertyType model : descriptor.properties())
        {
            properties.add(model.stateName());
        }
        store();
    }

    public LoadedDescriptor(Node descriptionNode)
    {
        this.descriptionNode = descriptionNode;
        load();
    }

    private void load()
    {
        String[] associations = (String[]) descriptionNode.getProperty(ASSOCIATIONS_PROPERTY_KEY);
        String[] properties = (String[]) descriptionNode.getProperty(ASSOCIATIONS_PROPERTY_KEY);
        String[] manyAssociations = (String[]) descriptionNode.getProperty(ASSOCIATIONS_PROPERTY_KEY);
        for (String association : associations)
        {
            this.associations.add(new StateName(association));
        }
        for (String property : properties)
        {
            this.properties.add(new StateName(property));
        }
        for (String association : manyAssociations)
        {
            String typeString = (String) descriptionNode.getProperty(FACTORY_TYPE_PROPERTY_PREFIX + association);
            this.manyAssociations.add(ManyAssociationFactory.load(new StateName(association), typeString));
        }
    }

    private void store()
    {
        String[] associations = new String[this.associations.size()];
        int idx = 0;
        for (StateName association : this.associations)
        {
            associations[idx++] = association.toString();
        }
        String[] properties = new String[this.properties.size()];
        idx = 0;
        for (StateName property : this.properties)
        {
            properties[idx++] = property.toString();
        }
        String[] manyAssociations = new String[this.manyAssociations.size()];
        int index = 0;
        for (ManyAssociationFactory factory : this.manyAssociations)
        {
            StateName qName = factory.getStateName();
            manyAssociations[index++] = qName.toString();
            descriptionNode.setProperty(FACTORY_TYPE_PROPERTY_PREFIX + qName, factory.typeString());
        }
        descriptionNode.setProperty(ASSOCIATIONS_PROPERTY_KEY, associations);
        descriptionNode.setProperty(PROPERTIES_PROPERTY_KEY, properties);
        descriptionNode.setProperty(MANY_ASSOCIATIONS_PROPERTY_KEY, manyAssociations);
    }

    private void verify(EntityType descriptor)
    {
        // TODO: implement means of verifying that the loaded descriprot matches the entity type
    }

    Iterable<ManyAssociationFactory> getManyAssociationFactories()
    {
        return new ImmutableIterable<ManyAssociationFactory>(manyAssociations);
    }

    Iterable<StateName> getPropertyNames()
    {
        return new ImmutableIterable<StateName>(properties);
    }

    Iterable<StateName> getAssociationNames()
    {
        return new ImmutableIterable<StateName>(associations);
    }

    Iterable<StateName> getManyAssociationNames()
    {
        return new Iterable<StateName>()
        {
            public Iterator<StateName> iterator()
            {
                final Iterator<ManyAssociationFactory> iter = manyAssociations.iterator();
                return new Iterator<StateName>()
                {
                    public boolean hasNext()
                    {
                        return iter.hasNext();
                    }

                    public StateName next()
                    {
                        return iter.next().getStateName();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    private static class ImmutableIterable<T> implements Iterable<T>
    {
        private final Iterable<T> original;

        public ImmutableIterable(Iterable<T> original)
        {
            this.original = original;
        }

        public Iterator<T> iterator()
        {
            final Iterator<T> iter = original.iterator();
            return new Iterator<T>()
            {
                public boolean hasNext()
                {
                    return iter.hasNext();
                }

                public T next()
                {
                    return iter.next();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
