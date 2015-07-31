/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.ide.plugin.idea.injections.structure.common;

import static java.util.Arrays.sort;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jStructureAnnotationConstants
{
    public static final String QUALIFIED_NAME_STRUCTURE_ANNOTATION = "org.qi4j.api.injection.scope.Structure";

    public static final String[] VALID_STRUCTURE_INJECTION_TYPE;

    static
    {
        VALID_STRUCTURE_INJECTION_TYPE = new String[]
            {
                "org.qi4j.composite.CompositeBuilderFactory",
                "org.qi4j.object.ObjectBuilderFactory",
                "org.qi4j.entity.UnitOfWorkFactory",
                "org.qi4j.service.ServiceFinder",
                "org.qi4j.structure.Module",
                "org.qi4j.structure.Layer",
                "org.qi4j.structure.Application",
                "org.qi4j.Qi4j",
                "org.qi4j.spi.Qi4jSPI"
            };
        sort( VALID_STRUCTURE_INJECTION_TYPE );
    }

    private Qi4jStructureAnnotationConstants()
    {
    }
}
