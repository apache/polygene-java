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
import org.qi4j.entitystore.sql.internal.H2SQLDatabaseSQLServiceMixin;
import org.sql.generation.api.vendor.H2Vendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * H2 EntityStore assembly.
 */
public class H2SQLEntityStoreAssembler
        extends AbstractSQLEntityStoreAssembler<H2SQLEntityStoreAssembler>
{

    @Override
    protected Class<?> getDatabaseSQLServiceSpecializationMixin()
    {
        return H2SQLDatabaseSQLServiceMixin.class;
    }

    @Override
    protected SQLVendor getSQLVendor()
            throws IOException
    {
        return SQLVendorProvider.createVendor( H2Vendor.class );
    }

}
