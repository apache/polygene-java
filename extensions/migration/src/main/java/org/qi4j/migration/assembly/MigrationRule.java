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

import java.io.IOException;
import org.qi4j.migration.Migrator;
import org.qi4j.spi.entitystore.helpers.StateStore;

/**
 * Migration rule that does not apply to a specific entity type
 */
public class MigrationRule
    extends AbstractMigrationRule
{
    private MigrationOperation operation;

    public MigrationRule( String fromVersion, String toVersion, MigrationOperation operation )
    {
        super( fromVersion, toVersion );
        this.operation = operation;
    }

    public void upgrade( StateStore stateStore, Migrator migrator )
        throws IOException
    {
        operation.upgrade( stateStore, migrator );
    }

    public void downgrade( StateStore stateStore, Migrator migrator )
        throws IOException
    {
        operation.downgrade( stateStore, migrator );
    }

    @Override
    public String toString()
    {
        return fromVersion + "->" + toVersion + ":" + operation;
    }
}
