/*
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.activation.ActivatorDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

/**
 * Activator Detail Descriptor.
 */
public class ActivatorDetailDescriptor
    implements InjectableDetailDescriptor
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
        validateNotNull( "Activator Descriptor", descriptor );
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
        validateNotNull( "ServiceDetailDescriptor", descriptor );
        service = descriptor;
    }

    final void setImportedService( ImportedServiceDetailDescriptor descriptor )
    {
        validateNotNull( "ImportedServiceDetailDescriptor", descriptor );
        importedService = descriptor;
    }

    final void setModule( ModuleDetailDescriptor descriptor )
    {
        validateNotNull( "ModuleDetailDescriptor", descriptor );
        module = descriptor;
    }

    final void setLayer( LayerDetailDescriptor descriptor )
    {
        validateNotNull( "LayerDetailDescriptor", descriptor );
        layer = descriptor;
    }

    final void setApplication( ApplicationDetailDescriptor descriptor )
    {
        validateNotNull( "ApplicationDetailDescriptor", descriptor );
        application = descriptor;
    }

    final void addConstructor( ConstructorDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ConstructorDetailDescriptor", descriptor );
        descriptor.setActivator( this );
        constructors.add( descriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "InjectedMethodDetailDescriptor", descriptor );
        descriptor.setActivator( this );
        injectedMethods.add( descriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor descriptor )
    {
        validateNotNull( "InjectedFieldDetailDescriptor", descriptor );
        descriptor.setActivator( this );
        injectedFields.add( descriptor );
    }

    @Override
    public String toString()
    {
        return descriptor.toString();
    }

}
