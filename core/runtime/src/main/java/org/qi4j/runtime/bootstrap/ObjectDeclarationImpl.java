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

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ObjectDeclaration;

/**
 * Declaration of an Object. Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#objects(Class[])}.
 */
public final class ObjectDeclarationImpl
    implements ObjectDeclaration
{
    private final Iterable<ObjectAssemblyImpl> assemblies;

    public ObjectDeclarationImpl( Iterable<ObjectAssemblyImpl> assemblies )
    {
        this.assemblies = assemblies;
    }

    @Override
    public ObjectDeclaration setMetaInfo( Object info )
    {
        for( ObjectAssemblyImpl assembly : assemblies )
        {
            assembly.metaInfo.set( info );
        }
        return this;
    }

    @Override
    public ObjectDeclaration visibleIn( Visibility visibility )
        throws IllegalStateException
    {
        for( ObjectAssemblyImpl assembly : assemblies )
        {
            assembly.visibility = visibility;
        }
        return this;
    }
}
