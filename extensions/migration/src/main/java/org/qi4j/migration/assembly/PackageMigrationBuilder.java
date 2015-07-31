/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.migration.assembly;

import org.qi4j.migration.operation.RenameEntity;

/**
 * JAVADOC
 */
public class PackageMigrationBuilder
{
    private MigrationBuilder builder;
    private VersionMigrationBuilder version;
    private String fromVersion;
    private String toVersion;
    private String fromName;
    private String toName;

    public PackageMigrationBuilder( MigrationBuilder builder,
                                    VersionMigrationBuilder version,
                                    String fromVersion,
                                    String toVersion,
                                    String fromName,
                                    String toName
    )
    {
        this.builder = builder;
        this.version = version;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.fromName = fromName;
        this.toName = toName;
    }

    public VersionMigrationBuilder end()
    {
        return version;
    }

    public PackageMigrationBuilder withEntities( String... entitySimpleNames )
    {
        for( String entitySimpleName : entitySimpleNames )
        {
            builder.entityMigrationRules().addRule( new EntityMigrationRule(
                fromVersion,
                toVersion,
                new String[]{ fromName + "." + entitySimpleName },
                new RenameEntity( fromName + "." + entitySimpleName, toName + "." + entitySimpleName ) ) );
        }

        return this;
    }
}
