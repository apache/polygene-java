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
package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.entitystore.sql.database.MySQLDatabaseSQLServiceMixin;
import org.qi4j.entitystore.sql.datasource.DataSourceService;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class MySQLEntityStoreAssembler
        extends AbstractSQLEntityStoreAssembler
{

    public static final String ENTITYSTORE_SERVICE_NAME = "entitystore-mysql";

    public static final String DATASOURCE_SERVICE_NAME = "datasource-mysql";

    public MySQLEntityStoreAssembler()
    {
        super();
    }

    public MySQLEntityStoreAssembler( Visibility visibility )
    {
        super( visibility );
    }

    public MySQLEntityStoreAssembler( DataSourceService importedDataSourceService )
    {
        super( importedDataSourceService );
    }

    public MySQLEntityStoreAssembler( Visibility visibility, DataSourceService importedDataSourceService )
    {
        super( visibility, importedDataSourceService );
    }

    public MySQLEntityStoreAssembler( Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        super( dataSourceServiceMixins );
    }

    public MySQLEntityStoreAssembler( Visibility visibility, Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        super( visibility, dataSourceServiceMixins );
    }

    @Override
    protected String getEntityStoreServiceName()
    {
        return ENTITYSTORE_SERVICE_NAME;
    }

    @Override
    protected String getDataSourceServiceName()
    {
        return DATASOURCE_SERVICE_NAME;
    }

    @Override
    protected Class<?> getDatabaseSQLServiceSpecializationMixin()
    {
        return MySQLDatabaseSQLServiceMixin.class;
    }

}
