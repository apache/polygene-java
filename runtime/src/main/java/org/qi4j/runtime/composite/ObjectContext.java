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

package org.qi4j.runtime.composite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.property.AbstractProperty;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.injection.ObjectInjectionContext;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectContext
{
    private ObjectBinding objectBinding;
    private InstanceFactory instanceFactory;
    private ModuleBinding moduleBinding;

    public ObjectContext( ObjectBinding objectBinding, ModuleBinding moduleBinding, InstanceFactory instanceFactory )
    {
        this.moduleBinding = moduleBinding;
        this.objectBinding = objectBinding;
        this.instanceFactory = instanceFactory;
    }

    public ObjectModel getObjectModel()
    {
        return objectBinding.getObjectResolution().getObjectModel();
    }

    public ObjectResolution getObjectResolution()
    {
        return objectBinding.getObjectResolution();
    }

    public ObjectBinding getObjectBinding()
    {
        return objectBinding;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }

    public Object newObjectInstance( ModuleInstance moduleInstance, Set adapt, Object decoratedObject, Map<String, Object> objectProperties )
    {
        Map<String, AbstractProperty> properties = new HashMap<String, AbstractProperty>(); // TODO

        ObjectInjectionContext objectInjectionContext = new ObjectInjectionContext( moduleInstance.getCompositeBuilderFactory(), moduleInstance.getObjectBuilderFactory(), moduleBinding, adapt, decoratedObject, properties );
        Object objectInstance = instanceFactory.newInstance( objectBinding, objectInjectionContext );

        // Return
        return objectInstance;
    }

    public void inject( Object instance, ModuleInstance moduleInstance, Set<Object> adaptContext, Object decoratedObject, Map<String, Object> objectProperties )
    {
        Map<String, AbstractProperty> properties = new HashMap<String, AbstractProperty>(); // TODO

        ObjectInjectionContext objectInjectionContext = new ObjectInjectionContext( moduleInstance.getCompositeBuilderFactory(), moduleInstance.getObjectBuilderFactory(), moduleBinding, adaptContext, decoratedObject, properties );
        instanceFactory.inject( instance, objectBinding, objectInjectionContext );
    }
}
