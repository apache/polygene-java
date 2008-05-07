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

package org.qi4j.structure;

import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Structure;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.service.ServiceFinder;

/**
 * API for interacting with a Module. Instances
 * of this can be accessed by using the {@link Structure}
 * injection scope.
 */
public interface Module
    extends ServiceFinder
{
    ImmutableProperty<String> name();

    Module findModuleForComposite( Class<? extends Composite> compositetype );

    Module findModuleForMixinType( Class<?> mixintype );

    Module findModuleForObject( Class<?> objecttype );

    boolean isPublic( Class<?> compositeOrObject );

    Class<? extends Composite> findCompositeType( Class<?> mixintype );

    Class findClass( String className )
        throws ClassNotFoundException;
}
