/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.dbInitializer;

import java.util.Properties;
import static org.qi4j.composite.NullArgumentException.validateNotNull;

/**
 * {@code DBInitializerInfo} represents information on the sql resources to initialize the database.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class DBInitializerInfo
{
    private final String dbURL;
    private final Properties connectionProperties;
    private final String schemaURL;
    private final String dbDataURL;

    /**
     * Construct an instance of {@code DBInitializerInfo}.
     *
     * @param aDBURL                The db url. This argument must not be {@code null}.
     * @param aConnectionProperties The db connection properties. This argument must not be {@code null}.
     * @param aSchemaURL            The schema url. This argument is optional.
     * @param aDBDataURL            The db data url. This argument is optional.
     * @throws IllegalArgumentException Thrown if one or both {@code aDBURL} and {@code aConnectionProperties} arguments
     *                                  are {@code null}.
     * @see java.sql.DriverManager#getConnection(String, Properties)
     * @since 0.1.0
     */
    public DBInitializerInfo(
        String aDBURL, Properties aConnectionProperties,
        String aSchemaURL, String aDBDataURL )
        throws IllegalArgumentException
    {
        validateNotNull( "aDBURL", aDBURL );
        validateNotNull( "aConnectionProperties", aConnectionProperties );

        dbURL = aDBURL;
        connectionProperties = aConnectionProperties;
        schemaURL = aSchemaURL;
        dbDataURL = aDBDataURL;
    }

    /**
     * Returns the db url.
     *
     * @return The db url.
     * @since 0.1.0
     */
    final String getDbURL()
    {
        return dbURL;
    }

    /**
     * Returns the connection properties.
     *
     * @return The connection properties.
     * @since 0.1.0
     */
    final Properties getConnectionProperties()
    {
        return connectionProperties;
    }


    /**
     * Returns the schema url resource.
     *
     * @return The schema url resource.
     * @since 0.1.0
     */
    final String getSchemaURL()
    {
        return schemaURL;
    }

    /**
     * Returns the db data url.
     *
     * @return The db data url.
     * @since 0.1.0
     */
    final String getDataURL()
    {
        return dbDataURL;
    }


}