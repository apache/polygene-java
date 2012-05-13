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
package org.qi4j.library.sql.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtil
{

    public static void closeQuietly( ResultSet resultSet )
    {
        if ( resultSet != null ) {
            try {
                resultSet.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void closeQuietly( Statement select )
    {
        if ( select != null ) {
            try {
                select.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void closeQuietly( Connection connection )
    {
        if ( connection != null ) {
            try {
                connection.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void rollbackQuietly( Connection connection )
    {
        if ( connection != null ) {
            try {
                connection.rollback();
            } catch ( SQLException ignored ) {
            }
        }
    }

    private SQLUtil()
    {
    }

}
