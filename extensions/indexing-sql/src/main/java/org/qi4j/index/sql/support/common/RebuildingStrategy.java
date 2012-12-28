/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.index.sql.support.common;

/**
 * Helper interface to let the user of SQL-Indexing decide when to re-build SQL schema. Rebuilding
 * means first deleting all tables that are going to be used, and then creating them. Useful for
 * tests or when your app is in dev mode.
 *
 * You can decide your application's re-building policy for example like this:
 * {@code mainModule.addServices(RebuildingStrategy.class).withMixins(RebuildingStrategy.AlwaysNeed.class);}
 * . This way your application will always re-build all index data at startup.
 *
 * @author Stanislav Muhametsin
 *
 */
public interface RebuildingStrategy
{
    boolean rebuildingRequired( String dbAppVersion, String currentAppVersion );

    /**
     * The re-building strategy which ALWAYS re-builds the database schema. Useful with tests and
     * during early development stage of application.
     *
     * @author Stanislav Muhametsin
     */
    public class AlwaysNeed implements RebuildingStrategy
    {
        @Override
        public boolean rebuildingRequired( String dbAppVersion, String currentAppVersion )
        {
            return true;
        }
    }

    /**
     * The re-building strategy which NEVER re-builds the database schema. Useful when current
     * Qi4j-related database schema structure must be preserved at all costs.
     *
     * @author Stanislav Muhametsin
     */
    public class NeverNeed implements RebuildingStrategy
    {
        @Override
        public boolean rebuildingRequired( String dbAppVersion, String currentAppVersion )
        {
            return false;
        }
    }

    /**
     * The re-building strategy, which re-builds everything when application version changes. Useful
     * when migration from versions is implemented, or when application structure changes along with
     * version (but not during same version).
     *
     * @author Stanislav Muhametsin
     */
    public class NeedOnChange implements RebuildingStrategy
    {
        public boolean rebuildingRequired( String dbAppVersion, String currentAppVersion )
        {
            return !dbAppVersion.equals( currentAppVersion );
        }
    }
}
