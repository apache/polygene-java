/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.activation.ActivatorDescriptor;
import org.apache.polygene.api.util.Visitable;
import org.apache.polygene.api.util.Visitor;

/**
 * Activator Detail Descriptor.
 */
public class ActivatorDetailDescriptor
    implements InjectableDetailDescriptor, Visitable<ActivatorDetailDescriptor>
{
    private final ActivatorDescriptor descriptor;
    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDetailDescriptor> injectedFields;
    private ServiceDetailDescriptor service;
    private ImportedServiceDetailDescriptor importedService;
    private ModuleDetailDescriptor module;
    private LayerDetailDescriptor layer;
    private ApplicationDetailDescriptor application;

    public ActivatorDetailDescriptor( ActivatorDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "Activator Descriptor" );
        this.descriptor = descriptor;
        constructors = new LinkedList<>();
        injectedMethods = new LinkedList<>();
        injectedFields = new LinkedList<>();
    }

    /**
     * @return Service that own this {@code ActivatorDetailDescriptor}.
     */
    public ServiceDetailDescriptor service()
    {
        return service;
    }

    /**
     * @return Imported Service that own this {@code ActivatorDetailDescriptor}.
     */
    public ImportedServiceDetailDescriptor importedService()
    {
        return importedService;
    }

    /**
     * @return Module that own this {@code ActivatorDetailDescriptor}.
     */
    public ModuleDetailDescriptor module()
    {
        return module;
    }

    /**
     * @return Layer that own this {@code ActivatorDetailDescriptor}.
     */
    public LayerDetailDescriptor layer()
    {
        return layer;
    }

    /**
     * @return Application that own this {@code ActivatorDetailDescriptor}.
     */
    public ApplicationDetailDescriptor application()
    {
        return application;
    }

    @Override
    public Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    @Override
    public Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    @Override
    public Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

    final void setService( ServiceDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ServiceDetailDescriptor" );
        service = descriptor;
    }

    final void setImportedService( ImportedServiceDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ImportedServiceDetailDescriptor" );
        importedService = descriptor;
    }

    final void setModule( ModuleDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ModuleDetailDescriptor" );
        module = descriptor;
    }

    final void setLayer( LayerDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "LayerDetailDescriptor" );
        layer = descriptor;
    }

    final void setApplication( ApplicationDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ApplicationDetailDescriptor" );
        application = descriptor;
    }

    final void addConstructor( ConstructorDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ConstructorDetailDescriptor" );
        descriptor.setActivator( this );
        constructors.add( descriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "InjectedMethodDetailDescriptor" );
        descriptor.setActivator( this );
        injectedMethods.add( descriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "InjectedFieldDetailDescriptor" );
        descriptor.setActivator( this );
        injectedFields.add( descriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super ActivatorDetailDescriptor, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }

    @Override
    public String toString()
    {
        return descriptor.toString();
    }

    public JsonObject toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if( service() != null )
        {
            builder.add( "service", service().toString() );
        }
        if( importedService() != null )
        {
            builder.add( "importedService", importedService().toString() );
        }
        if( module() != null )
        {
            builder.add( "module", module().toString() );
        }
        if( layer() != null )
        {
            builder.add( "layer", layer().toString() );
        }
        if( application() != null )
        {
            builder.add( "application", application().toString() );
        }
        return builder.build();
    }
}
