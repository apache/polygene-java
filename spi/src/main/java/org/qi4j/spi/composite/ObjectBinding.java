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

package org.qi4j.spi.composite;

import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;

/**
 * Base class for object model bindings. Bindings are resolutions whose injections have been bound to injection providers.
 */
public final class ObjectBinding
    extends AbstractBinding
    implements StateBinding
{
    private Iterable<PropertyBinding> propertyBindings;
    private Iterable<AssociationBinding> associationBindings;

    public ObjectBinding( AbstractResolution abstractResolution, ConstructorBinding constructorBinding, Iterable<FieldBinding> fieldBindings, Iterable<MethodBinding> methodBindings, Iterable<PropertyBinding> propertyBindings, Iterable<AssociationBinding> associationBindings )
    {
        super( abstractResolution, constructorBinding, fieldBindings, methodBindings );
        this.associationBindings = associationBindings;
        this.propertyBindings = propertyBindings;
    }

    public ObjectResolution getObjectResolution()
    {
        return (ObjectResolution) getAbstractResolution();
    }

    public Iterable<PropertyBinding> getPropertyBindings()
    {
        return propertyBindings;
    }

    public Iterable<AssociationBinding> getAssociationBindings()
    {
        return associationBindings;
    }
}
