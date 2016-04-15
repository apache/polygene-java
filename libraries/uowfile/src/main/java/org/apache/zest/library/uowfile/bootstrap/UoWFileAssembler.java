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
package org.apache.zest.library.uowfile.bootstrap;

import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.uowfile.internal.UoWFileFactory;
import org.apache.zest.library.uowfile.plural.HasUoWFiles;
import org.apache.zest.library.uowfile.plural.UoWFilesLocator;
import org.apache.zest.library.uowfile.singular.HasUoWFile;
import org.apache.zest.library.uowfile.singular.UoWFileLocator;

/**
 * Add services needed for {@link HasUoWFile} and {@link HasUoWFiles}.
 * 
 * You usually want to use this assembler on the module where your entities belong.
 * 
 * Your entity types must extends either {@link HasUoWFile} or {@link HasUoWFiles}.
 * Their mixins must respectively implements {@link UoWFileLocator} and {@link UoWFilesLocator}
 */
public class UoWFileAssembler
    extends Assemblers.Visibility<UoWFileAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UoWFileFactory.class ).visibleIn( visibility() );
    }
}
