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

package org.apache.zest.runtime.composite;

import java.util.List;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.spi.module.ModuleSpi;

/**
 * Model for Transient Composites
 */
public class TransientModel extends CompositeModel
    implements TransientDescriptor
{
    public TransientModel( ModuleDescriptor module,
                           List<Class<?>> types, final Visibility visibility,
                           final MetaInfo metaInfo,
                           final MixinsModel mixinsModel,
                           final StateModel stateModel,
                           final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( module, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    public TransientInstance newInstance( UsesInstance uses,
                                          TransientStateInstance state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        TransientInstance compositeInstance = new TransientInstance( this, mixins, state );

        // Instantiate all mixins
        int i = 0;
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        // Return
        return compositeInstance;
    }

    public void checkConstraints( TransientStateInstance instanceState )
        throws ConstraintViolationException
    {
        stateModel.properties().forEach( propertyModel ->
            propertyModel.checkConstraints( instanceState.propertyFor( propertyModel.accessor() ).get() )
        );
    }
}