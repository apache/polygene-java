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

package org.apache.zest.library.rest.common;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;

import static org.apache.zest.api.util.Classes.*;
import static org.apache.zest.bootstrap.ClassScanner.*;
import static org.apache.zest.functional.Iterables.*;

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
