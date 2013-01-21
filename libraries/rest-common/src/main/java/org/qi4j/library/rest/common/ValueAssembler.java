/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.common;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import static org.qi4j.api.util.Classes.*;
import static org.qi4j.bootstrap.ClassScanner.*;
import static org.qi4j.functional.Iterables.*;

/**
 * Assembler for all REST values.
 */
public class ValueAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        for( Class<?> aClass : filter( isAssignableFrom( ValueComposite.class ), findClasses( Resource.class ) ))
        {
            module.values( aClass ).visibleIn( Visibility.application );
        }
    }
}
