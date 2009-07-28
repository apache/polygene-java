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

package org.qi4j.runtime.bootstrap.migration;

import org.qi4j.bootstrap.EntityMigrationDeclaration;
import org.qi4j.bootstrap.Migrator;

/**
 * JAVADOC
 */
public class EntityMigrationDeclarationImpl
        implements EntityMigrationDeclaration
{
    private MigrationImpl migration;
    private String fromVersion;
    private String toVersion;

    public EntityMigrationDeclarationImpl(String fromVersion, String toVersion, MigrationImpl migration)
    {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.migration = migration;
    }

    public EntityMigrationDeclarationImpl(MigrationImpl migration)
    {
        this.migration = migration;
    }

    public EntityMigrationDeclaration isSame()
    {
        migration.addRule(fromVersion, toVersion, new IsSameRule());
        return this;
    }

    public EntityMigrationDeclaration renamedProperty(String from, String to)
    {
        return this;
    }

    public EntityMigrationDeclaration addedProperty(String stateName, String defaultValue)
    {
        return this;
    }

    public EntityMigrationDeclaration addedProperty(String stateName, Migrator migrator)
    {
        return this;
    }

    public EntityMigrationDeclaration removedProperty(String stateName)
    {
        return this;
    }

    public EntityMigrationDeclaration custom(Migrator migrator)
    {
        return this;
    }
}
