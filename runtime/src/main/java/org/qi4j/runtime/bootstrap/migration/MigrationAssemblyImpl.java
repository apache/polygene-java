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

import org.qi4j.bootstrap.MigrationAssembly;
import org.qi4j.bootstrap.EntityMigrationDeclaration;

/**
 * JAVADOC
 */
public class MigrationAssemblyImpl
    implements MigrationAssembly
{
    MigrationImpl migration = new MigrationImpl();

    public EntityMigrationDeclaration migrate(String fromVersion, String toVersion)
    {
        return new EntityMigrationDeclarationImpl(fromVersion, toVersion, migration);
    }

    public EntityMigrationDeclaration defaultMigration()
    {
        return new EntityMigrationDeclarationImpl(migration);
    }

    public MigrationImpl getMigration()
    {
        return migration;
    }
}
