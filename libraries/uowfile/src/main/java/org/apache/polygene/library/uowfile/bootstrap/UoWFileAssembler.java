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
package org.apache.polygene.library.uowfile.bootstrap;

import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.uowfile.internal.UoWFileFactory;
import org.apache.polygene.library.uowfile.plural.HasUoWFiles;
import org.apache.polygene.library.uowfile.plural.UoWFilesLocator;
import org.apache.polygene.library.uowfile.singular.HasUoWFile;
import org.apache.polygene.library.uowfile.singular.UoWFileLocator;

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
    {
        super.assemble( module );
        module.services( UoWFileFactory.class ).visibleIn( visibility() );
    }
}
