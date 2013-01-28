/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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

package org.qi4j.runtime.service;

import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.service.ServiceImporter;

/**
 * JAVADOC
 */
public final class ImportedServiceInstance<T>
    implements Activation
{
    private final T instance;
    private final ServiceImporter<T> importer;

    public ImportedServiceInstance( T instance, ServiceImporter<T> importer )
    {
        this.importer = importer;
        this.instance = instance;
    }

    public T instance()
    {
        return instance;
    }

    public ServiceImporter importer()
    {
        return importer;
    }

    public boolean isAvailable()
    {
        return importer.isAvailable( instance );
    }

    @Override
    public void activate()
        throws ActivationException
    {
        // NOOP
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        // NOOP
    }
}