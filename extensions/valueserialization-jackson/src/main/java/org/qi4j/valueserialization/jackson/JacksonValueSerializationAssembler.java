/*
 * Copyright (c) 2013, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.valueserialization.jackson;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Function;

/**
 * Assemble a ValueSerialization Service producing and consuming JSON documents.
 */
public class JacksonValueSerializationAssembler
    implements Assembler
{

    private Visibility visibility = Visibility.module;
    private Function<Application, Module> valuesModuleFinder;

    public JacksonValueSerializationAssembler visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public JacksonValueSerializationAssembler withValuesModuleFinder( Function<Application, Module> valuesModuleFinder )
    {
        this.valuesModuleFinder = valuesModuleFinder;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( valuesModuleFinder == null )
        {
            module.services( JacksonValueSerializationService.class ).
                visibleIn( visibility ).
                taggedWith( ValueSerialization.Formats.JSON );
        }
        else
        {
            module.services( JacksonValueSerializationService.class ).
                visibleIn( visibility ).
                taggedWith( ValueSerialization.Formats.JSON ).
                setMetaInfo( valuesModuleFinder );
        }
    }
}
