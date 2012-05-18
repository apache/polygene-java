/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.uowfile.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.uowfile.internal.UoWFileFactory;
import org.qi4j.library.uowfile.plural.HasUoWFiles;
import org.qi4j.library.uowfile.plural.UoWFilesLocator;
import org.qi4j.library.uowfile.singular.HasUoWFile;
import org.qi4j.library.uowfile.singular.UoWFileLocator;

/**
 * Add services needed for {@link HasUoWFile} and {@link HasUoWFiles}.
 * 
 * You usually want to use this assembler on the module where your entities belong.
 * 
 * Your entity types must extends either {@link HasUoWFile} or {@link HasUoWFiles}.
 * Their mixins must respectively implements {@link UoWFileLocator} and {@link UoWFilesLocator}
 */
public class UoWFileAssembler
        implements Assembler
{

    private final Visibility visibility;

    public UoWFileAssembler()
    {
        this.visibility = Visibility.module;
    }

    public UoWFileAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( UoWFileFactory.class ).visibleIn( visibility );
    }

}
