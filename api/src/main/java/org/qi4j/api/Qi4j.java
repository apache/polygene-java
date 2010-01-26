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

import org.qi4j.api.composite.Composite;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

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
     *
     * @return the dereferenced Composite
     */
    <T> T dereference( T composite );

    /**
     * Get the super Composite of the given Composite. <p>If one Composite
     * type MyComposite is extended by CustomMyComposite interface,
     * then the MyComposite is considered to be the super Composite
     * of CustomMyComposite. A Composite may only extend one other Composite,
     * but may extend any number of other interfaces which do not in turn
     * extend Composite.</p>
     *
     * <p>If there are multiple super composites, this method will only return the first
     * one found.</p>
     *
     * @param compositeClass the Composite type whose super Composite should be returned
     *
     * @return the super Composite of the given Composite, or null if it does not have one
     */
    <S extends Composite, T extends S> Class<S> getSuperComposite( Class<T> compositeClass );

    /**
     * Finds the Configuration instance of a service.
     * <p>This is used by ConfigurationMixin to figure out the configuration instance used by
     * a Service using {@link org.qi4j.api.configuration.Configuration}, and should not be
     * used directly by client code.</p>
     *
     * <p>If the Configuration entity doesn't exist in the visible EntityStore, then a properties
     * file with the name of the service identifier will be located on the classpath, and the
     * values used to create the Configuration instance, which will then be saved to the EntityStore
     * for future use. That means that the properties file is <b>only</b> used
     *
     * @param serviceComposite the service instance
     * @param uow              the UnitOfWork from which the configuration will be loaded
     *
     * @return configuration instance
     *
     * @throws InstantiationException thrown if the configuration cannot be instantiated
     */
    <T> T getConfigurationInstance( ServiceComposite serviceComposite, UnitOfWork uow )
        throws InstantiationException;

    Class<?> getConfigurationType( Composite serviceComposite );

    /**
     * Returns the Module where the UnitOfWork belongs.
     *
     * @param uow The UnitOfWork to be checked.
     *
     * @return The Module instance where the UnitOfWork belongs.
     */
    Module getModule( UnitOfWork uow );

    /**
     * Returns the Module where the Composite belongs.
     *
     * @param composite The Composite to be checked.
     *
     * @return The Module instance where the Composite belongs.
     */
    Module getModule( Composite composite );

    /**
     * Returns the Module where the service is located.
     *
     * @param service The service to be checked.
     *
     * @return The Module instance where the Composite belongs.
     */
    Module getModule( ServiceReference service );
}
