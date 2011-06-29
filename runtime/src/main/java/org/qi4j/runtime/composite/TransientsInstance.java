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

package org.qi4j.runtime.composite;

import org.qi4j.api.common.Visibility;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.VisibilitySpecification;

import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.map;

/**
 * JAVADOC
 */
public class TransientsInstance
{
    private final TransientsModel transients;
    private final ModuleInstance moduleInstance;

    public TransientsInstance( TransientsModel transients, ModuleInstance moduleInstance )
    {
        this.transients = transients;
        this.moduleInstance = moduleInstance;
    }

    public TransientsModel model()
    {
        return transients;
    }

    public Iterable<ModelModule<TransientModel>> visibleTransients( Visibility visibility )
    {
        return map( ModelModule.<TransientModel>modelModuleFunction( moduleInstance ), filter( new VisibilitySpecification( visibility ), transients.models() ) );
    }
}
