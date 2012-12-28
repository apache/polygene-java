/*
 * Copyright (c) 2010, Stanislav Muhametsin.
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
package org.qi4j.index.sql.assembly;

import java.io.IOException;
import org.qi4j.index.sql.support.postgresql.PostgreSQLService;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

public class PostgreSQLIndexQueryAssembler
        extends AbstractSQLIndexQueryAssembler<PostgreSQLIndexQueryAssembler>
{

    @Override
    protected SQLVendor getSQLVendor()
            throws IOException
    {
        return SQLVendorProvider.createVendor( PostgreSQLVendor.class );
    }

    @Override
    protected Class<?> getIndexQueryServiceType()
    {
        return PostgreSQLService.class;
    }

}
