/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.sql.common;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.sql.assembly.DataSourceAssembler;

public abstract class AbstractSQLAssembler
        implements Assembler
{

    private static final Visibility DEFAULT_VISIBILITY = Visibility.module;

    private final Visibility visibility;

    private final DataSourceAssembler dsAss;

    public AbstractSQLAssembler( DataSourceAssembler assembler )
    {
        this( DEFAULT_VISIBILITY, assembler );
    }

    public AbstractSQLAssembler( Visibility visibility, DataSourceAssembler assembler )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        NullArgumentException.validateNotNull( "Data source assembler", assembler );

        this.visibility = visibility;
        this.dsAss = assembler;
    }

    protected DataSourceAssembler getDataSourceAssembler()
    {
        return this.dsAss;
    }

    protected Visibility getVisibility()
    {
        return this.visibility;
    }

    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {

        DataSourceAssembler dataSourceAssembler = this.getDataSourceAssembler();
        if ( dataSourceAssembler == null ) {
            throw new IllegalStateException(
                    "Unable to assemble SQLEntityStore, no importable DataSourceService nor DataSourceServiceMixin provided" );
        }

        dataSourceAssembler.assemble( module );

        this.doAssemble( module );

    }

    protected abstract void doAssemble( ModuleAssembly module )
            throws AssemblyException;

}
