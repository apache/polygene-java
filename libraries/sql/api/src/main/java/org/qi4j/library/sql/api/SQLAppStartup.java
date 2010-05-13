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


package org.qi4j.library.sql.api;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is the interface which is called by SQL-Indexing when it is activated. This allows implementation specific initializations for RDBMS (for example,
 * possibly re-building database structure).
 * 
 * @author Stanislav Muhametsin
 */
public interface SQLAppStartup
{
   
   /**
    * This method is called when connection needs to be done. Make sure not to make much initializations here, as this is only to get
    * connection reference to SQL-indexing.
    * @return Connection to database.
    * @throws SQLException If SQL.
    */
   Connection createConnection() throws SQLException;
   
   /**
    * This method is called when connection may be safely initialized - for example, possibly (re-)building database structure.
    * @param connection Connection to initialize.
    * @throws SQLException If SQL.
    */
   void initConnection(Connection connection) throws SQLException;
   
}
