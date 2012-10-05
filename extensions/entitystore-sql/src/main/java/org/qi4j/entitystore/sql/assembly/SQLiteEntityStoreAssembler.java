/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.entitystore.sql.assembly;

import java.io.IOException;
import org.qi4j.api.common.Visibility;
import org.qi4j.entitystore.sql.internal.SQLiteDatabaseSQLServiceMixin;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;
import org.sql.generation.api.vendor.SQLiteVendor;

public class SQLiteEntityStoreAssembler
        extends AbstractSQLEntityStoreAssembler
{

    public static final String ENTITYSTORE_SERVICE_NAME = "entitystore-sqlite";

    public SQLiteEntityStoreAssembler( DataSourceAssembler assembler )
    {
        super( assembler );
    }

    public SQLiteEntityStoreAssembler( Visibility visibility, DataSourceAssembler assembler )
    {
        super( visibility, assembler );
    }

    @Override
    protected String getEntityStoreServiceName()
    {
        return ENTITYSTORE_SERVICE_NAME;
    }

    @Override
    protected Class<?> getDatabaseSQLServiceSpecializationMixin()
    {
        return SQLiteDatabaseSQLServiceMixin.class;
    }

    @Override
    protected SQLVendor getSQLVendor()
            throws IOException
    {
        return SQLVendorProvider.createVendor( SQLiteVendor.class );
    }

}
