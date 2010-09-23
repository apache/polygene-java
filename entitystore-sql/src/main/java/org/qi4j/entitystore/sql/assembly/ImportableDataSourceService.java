/*
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
package org.qi4j.entitystore.sql.assembly;

import javax.sql.DataSource;
import org.qi4j.entitystore.sql.internal.database.SQLs;
import org.qi4j.entitystore.sql.internal.datasource.DataSourceService;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class ImportableDataSourceService
    implements DataSourceService
{

    private final DataSource dataSource;

    public ImportableDataSourceService( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }

    public String getConfiguredShemaName()
    {
        return SQLs.DEFAULT_SCHEMA_NAME;
    }

}
