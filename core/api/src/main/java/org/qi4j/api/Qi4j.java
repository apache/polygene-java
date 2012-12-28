/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;

/**
 * Encapsulation of the Qi4j API.
 */
public interface Qi4j
{
    /**
     * If a Modifier gets a reference to the Composite using @This,
     * then that reference must be dereferenced using this method
     * before handing it out for others to use.
     *
     * @param composite instance reference injected in Modified using @This
     * @return the dereferenced Composite
     */
    <T> T dereference( T composite );

    /**
     * Returns the Module or UnitOfWork where the Composite belongs.
     *
     * @param compositeOrUow The Composite (Service, Value, Entity or Transient) or UnitOfWork to lookup the Module it
     *                       belongs to.
     * @return The Module instance where the Composite or UnitOfWork belongs to.
     */
    Module getModule( Object compositeOrUow );

    /**
     * Returns the ModelDescriptor of the Composite.
     *
     * @param compositeOrServiceReference The Composite (Service, Value, Entity or Transient) for which to lookup the
     *                                    ModelDescriptor
     * @return The ModelDescriptor of the Composite
     */
    ModelDescriptor getModelDescriptor( Object compositeOrServiceReference );

    /**
     * Returns the CompositeDescriptor of the Composite.
     *
     * @param compositeOrServiceReference The Composite (Service, Value, Entity or Transient) for which to lookup the
     *                                    CompositeDescriptor
     * @return The CompositeDescriptor of the Composite
     */
    CompositeDescriptor getCompositeDescriptor( Object compositeOrServiceReference );

    /**
     * Returns the TransientDescriptor of the TransientComposite.
     *
     * @param transsient The TransientComposite for which to lookup the TransientDescriptor
     * @return The TransientDescriptor of the TransientComposite
     */
    TransientDescriptor getTransientDescriptor( Object transsient );

    /**
     * Returns the EntityDescriptor of the EntityComposite.
     *
     * @param entity The EntityComposite for which to lookup the EntityDescriptor
     * @return The EntityDescriptor of the EntityComposite
     */
    EntityDescriptor getEntityDescriptor( Object entity );

    /**
     * Returns the ValueDescriptor of the ValueComposite.
     *
     * @param value The ValueComposite for which to lookup the ValueDescriptor
     * @return The ValueDescriptor of the ValueComposite
     */
    ValueDescriptor getValueDescriptor( Object value );

    /**
     * Returns the ServiceDescriptor of the ServiceComposite.
     *
     * @param service The ServiceComposite for which to lookup the ServiceDescriptor
     * @return The ServiceDescriptor of the ServiceComposite
     */
    ServiceDescriptor getServiceDescriptor( Object service );

    /**
     * Returns the PropertyDescriptor of the Property.
     *
     * @param property The Property for which to lookup the PropertyDescriptor
     * @return The PropertyDescriptor of the Property
     */
    PropertyDescriptor getPropertyDescriptor( Property property );

    /**
     * Returns the AssociationDescriptor of the Association.
     *
     * @param association The Association for which to lookup the AssociationDescriptor
     * @return The AssociationDescriptor of the Association
     */
    AssociationDescriptor getAssociationDescriptor( AbstractAssociation association );

    /**
     * Function that returns the CompositeDescriptor of a Composite.
     */
    Function<Composite, CompositeDescriptor> DESCRIPTOR_FUNCTION = new Function<Composite, CompositeDescriptor>()
    {
        @Override
        public CompositeDescriptor map( Composite composite )
        {
            if( composite instanceof Proxy )
            {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler( composite );
                return ( (CompositeInstance) invocationHandler ).descriptor();
            }
            else
            {
                try
                {
                    CompositeInstance instance = (CompositeInstance) composite.getClass()
                        .getField( "_instance" )
                        .get( composite );
                    return instance.descriptor();
                }
                catch( Exception e )
                {
                    throw (InvalidCompositeException) new InvalidCompositeException( "Could not get _instance field" ).initCause( e );
                }
            }
        }
    };

    /**
     * Function that returns the CompositeInstance of a Composite.
     */
    Function<Composite, CompositeInstance> INSTANCE_FUNCTION = new Function<Composite, CompositeInstance>()
    {
        @Override
        public CompositeInstance map( Composite composite )
        {
            if( composite instanceof Proxy )
            {
                return ( (CompositeInstance) Proxy.getInvocationHandler( composite ) );
            }
            else
            {
                try
                {
                    CompositeInstance instance = (CompositeInstance) composite.getClass()
                        .getField( "_instance" )
                        .get( composite );
                    return instance;
                }
                catch( Exception e )
                {
                    throw (InvalidCompositeException) new InvalidCompositeException( "Could not get _instance field" ).initCause( e );
                }
            }
        }
    };
}
