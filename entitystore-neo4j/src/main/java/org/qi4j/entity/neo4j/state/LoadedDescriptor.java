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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.neo4j.api.core.Node;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.CompositeDescriptor;

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

    public static LoadedDescriptor loadDescriptor( CompositeDescriptor descriptor, Node descriptionNode )
    {
        LoadedDescriptor result = cache.get( descriptionNode );
        if( result == null )
        {
            synchronized( cache )
            {
                result = cache.get( descriptionNode );
                if( result == null )
                {
                    result = new LoadedDescriptor( descriptor, descriptionNode );
                    cache.put( descriptionNode, result );
                }
            }
        }
        result.verify( descriptor );
        return result;
    }

    public static LoadedDescriptor loadDescriptor( Node descriptionNode )
    {
        LoadedDescriptor result = cache.get( descriptionNode );
        if( result == null )
        {
            synchronized( cache )
            {
                result = cache.get( descriptionNode );
                if( result == null )
                {
                    result = new LoadedDescriptor( descriptionNode );
                    cache.put( descriptionNode, result );
                }
            }
        }
        return result;
    }

    private final Node descriptionNode;
    private final List<ManyAssociationFactory> manyAssociations = new LinkedList<ManyAssociationFactory>();
    private final List<String> associations = new LinkedList<String>();
    private final List<String> properties = new LinkedList<String>();

    private LoadedDescriptor( CompositeDescriptor descriptor, Node descriptionNode )
    {
        this.descriptionNode = descriptionNode;
        for( AssociationModel model : descriptor.getCompositeModel().getAssociationModels() )
        {
            if( ManyAssociationFactory.isManyAssociation( model ) )
            {
                manyAssociations.add( ManyAssociationFactory.getFactory( model ) );
            }
            else
            {
                associations.add( model.getQualifiedName() );
            }
        }
        for( PropertyModel model : descriptor.getCompositeModel().getPropertyModels() )
        {
            properties.add( model.getQualifiedName() );
        }
        store();
    }

    public LoadedDescriptor( Node descriptionNode )
    {
        this.descriptionNode = descriptionNode;
        load();
    }

    private void load()
    {
        String[] associations = (String[]) descriptionNode.getProperty( ASSOCIATIONS_PROPERTY_KEY );
        String[] properties = (String[]) descriptionNode.getProperty( ASSOCIATIONS_PROPERTY_KEY );
        String[] manyAssociations = (String[]) descriptionNode.getProperty( ASSOCIATIONS_PROPERTY_KEY );
        this.associations.addAll( Arrays.asList( associations ) );
        this.properties.addAll( Arrays.asList( properties ) );
        for( String association : manyAssociations )
        {
            String typeString = (String) descriptionNode.getProperty( FACTORY_TYPE_PROPERTY_PREFIX + association );
            this.manyAssociations.add( ManyAssociationFactory.load( association, typeString ) );
        }
    }

    private void store()
    {
        String[] associations = new String[this.associations.size()];
        associations = this.associations.toArray( associations );
        String[] properties = new String[this.properties.size()];
        properties = this.properties.toArray( properties );
        String[] manyAssociations = new String[this.manyAssociations.size()];
        int index = 0;
        for( ManyAssociationFactory factory : this.manyAssociations )
        {
            String qName = factory.getQualifiedName();
            manyAssociations[ index++ ] = qName;
            descriptionNode.setProperty( FACTORY_TYPE_PROPERTY_PREFIX + qName, factory.typeString() );
        }
        descriptionNode.setProperty( ASSOCIATIONS_PROPERTY_KEY, associations );
        descriptionNode.setProperty( PROPERTIES_PROPERTY_KEY, properties );
        descriptionNode.setProperty( MANY_ASSOCIATIONS_PROPERTY_KEY, manyAssociations );
    }

    private void verify( CompositeDescriptor descriptor )
    {
        // TODO: implement means of verifying that the loaded descriprot matches the composite descriptor
    }

    Iterable<ManyAssociationFactory> getManyAssociationFactories()
    {
        return new ImmutableIterable<ManyAssociationFactory>( manyAssociations );
    }

    Iterable<String> getPropertyNames()
    {
        return new ImmutableIterable<String>( properties );
    }

    Iterable<String> getAssociationNames()
    {
        return new ImmutableIterable<String>( associations );
    }

    Iterable<String> getManyAssociationNames()
    {
        return new Iterable<String>()
        {
            public Iterator<String> iterator()
            {
                final Iterator<ManyAssociationFactory> iter = manyAssociations.iterator();
                return new Iterator<String>()
                {
                    public boolean hasNext()
                    {
                        return iter.hasNext();
                    }

                    public String next()
                    {
                        return iter.next().getQualifiedName();
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

        public ImmutableIterable( Iterable<T> original )
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
