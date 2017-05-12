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
package org.apache.polygene.entitystore.jooq.assembly;

import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.jooq.JooqEntityStoreConfiguration;
import org.apache.polygene.entitystore.jooq.JooqEntityStoreService;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

/**
 * MySQL EntityStore assembly.
 */
public class JooqEntityStoreAssembler extends Assemblers.VisibilityIdentityConfig<JooqEntityStoreAssembler>
    implements Assembler
{
    public static final Identity DEFAULT_ENTITYSTORE_IDENTITY = new StringIdentity( "entitystore-jooq" );

    @Override
    public void assemble( ModuleAssembly module )
    {
        Settings settings = getSettings();
        if( settings == null )
        {
            throw new AssemblyException( "Settings must not be null" );
        }

        String identity = ( hasIdentity() ? identity() : DEFAULT_ENTITYSTORE_IDENTITY ).toString();

        module.services( JooqEntityStoreService.class )
              .identifiedBy( identity )
              .visibleIn( visibility() )
              .setMetaInfo( getSQLDialect() )
              .setMetaInfo( settings );

        if( hasConfig() )
        {
            configModule().entities( JooqEntityStoreConfiguration.class ).visibleIn( configVisibility() );
        }
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
