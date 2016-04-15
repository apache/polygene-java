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

package org.apache.zest.runtime.bootstrap;

import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.bootstrap.TransientAssembly;
import org.apache.zest.runtime.composite.TransientModel;

/**
 * Declaration of a TransientComposite.
 */
public final class TransientAssemblyImpl extends CompositeAssemblyImpl
    implements TransientAssembly
{
    public TransientAssemblyImpl( Class<?> transientType )
    {
        super( transientType );

        // The composite must always implement TransientComposite, as a marker interface
        if( !TransientComposite.class.isAssignableFrom( transientType ) )
        {
            types.add( TransientComposite.class );
        }

        // If type is a class, register it as a mixin
        if( !transientType.isInterface() )
        {
            mixins.add( transientType );
        }
    }

    TransientModel newTransientModel( ModuleDescriptor module,
                                      StateDeclarations stateDeclarations,
                                      AssemblyHelper helper
    )
    {
        try
        {
            buildComposite( helper, stateDeclarations );
            return new TransientModel(
                module, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }
}
