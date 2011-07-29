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
import org.qi4j.bootstrap.TransientDeclaration;

import java.util.Arrays;

/**
 * Declaration of a Composite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#transients(Class[])}.
 */
public final class TransientDeclarationImpl
    implements TransientDeclaration
{
    private Iterable<TransientAssemblyImpl> assemblies;

    public TransientDeclarationImpl( Iterable<TransientAssemblyImpl> assemblies)
    {
        this.assemblies = assemblies;
    }

    public TransientDeclaration setMetaInfo( Object info )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.metaInfo.set( info );
        }
        return this;
    }

    public TransientDeclaration visibleIn( Visibility visibility )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.visibility = visibility;
        }
        return this;
    }

    public TransientDeclaration withConcerns( Class<?>... concerns )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.concerns.addAll( Arrays.asList( concerns ) );
        }
        return this;
    }

    public TransientDeclaration withSideEffects( Class<?>... sideEffects )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.sideEffects.addAll( Arrays.asList( sideEffects ) );
        }
        return this;
    }

    public TransientDeclaration withMixins( Class<?>... mixins )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.mixins.addAll( Arrays.asList( mixins ) );
        }
        return this;
    }

    public TransientDeclaration withTypes( Class<?>... types )
    {
        for( TransientAssemblyImpl assembly : assemblies )
        {
            assembly.types.addAll( Arrays.asList( types ) );
        }
        return this;
    }
}
