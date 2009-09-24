/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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
public class VersionMigrationBuilder
{
    MigrationRules rules;

    String fromVersion;
    String toVersion;

    public VersionMigrationBuilder( MigrationRules rules, String fromVersion, String toVersion )
    {
        this.rules = rules;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    public VersionMigrationBuilder toVersion( String toVersion )
    {
        return new VersionMigrationBuilder( rules, toVersion, toVersion);
    }

    public VersionMigrationBuilder renameEntity( String fromName, String toName )
    {
        rules.addRule( new MigrationRule(fromVersion, toVersion, new RenameEntity(fromName, toName) ));

        return this;
    }
    public EntityMigrationBuilder forEntities(String... entityTypes)
    {
        return new EntityMigrationBuilder(this, entityTypes);
    }

}