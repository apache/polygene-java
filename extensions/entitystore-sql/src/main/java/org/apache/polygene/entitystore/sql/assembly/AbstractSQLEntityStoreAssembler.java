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
 */
package org.apache.polygene.entitystore.sql.assembly;

import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.SQLEntityStoreConfiguration;
import org.apache.polygene.entitystore.sql.SQLEntityStoreService;
import org.apache.polygene.library.sql.liquibase.LiquibaseAssembler;
import org.apache.polygene.library.sql.liquibase.LiquibaseConfiguration;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

/**
 * Base SQL EntityStore assembly.
 */
public abstract class AbstractSQLEntityStoreAssembler<AssemblerType>
    extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{
    public static final Identity DEFAULT_ENTITYSTORE_IDENTITY = StringIdentity.identity( "entitystore-sql" );
    private static final String DEFAULT_CHANGELOG_PATH = "org/apache/polygene/entitystore/sql/changelog.xml";

    private String changelogPath = DEFAULT_CHANGELOG_PATH;

    @Override
    public void assemble( ModuleAssembly module )
    {
        SQLDialect dialect = getSQLDialect();
        if( dialect == null )
        {
            throw new AssemblyException( "SQLDialect must not be null" );
        }
        Settings settings = getSettings();
        if( settings == null )
        {
            throw new AssemblyException( "Settings must not be null" );
        }

        String identity = ( hasIdentity() ? identity() : DEFAULT_ENTITYSTORE_IDENTITY ).toString();

        LiquibaseAssembler liquibase = new LiquibaseAssembler().identifiedBy( identity + "-liquibase" );
        if( hasConfig() )
        {
            liquibase.withConfig( configModule(), configVisibility() );
            LiquibaseConfiguration liquibaseconfig = configModule().forMixin( LiquibaseConfiguration.class )
                                                                   .declareDefaults();
            liquibaseconfig.changeLog().set( changelogPath );
        }
        liquibase.assemble( module );

        module.services( SQLEntityStoreService.class )
              .identifiedBy( identity )
              .visibleIn( visibility() )
              .setMetaInfo( dialect )
              .setMetaInfo( settings );

        if( hasConfig() )
        {
            configModule().entities( SQLEntityStoreConfiguration.class ).visibleIn( configVisibility() );
        }
    }

    public AssemblerType withLiquibaseChangelog( String changelogPath )
    {
        this.changelogPath = changelogPath;
        return (AssemblerType) this;
    }

    protected Settings getSettings()
    {
        return new Settings().withRenderNameStyle( RenderNameStyle.QUOTED );
    }

    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.DEFAULT;
    }
}
