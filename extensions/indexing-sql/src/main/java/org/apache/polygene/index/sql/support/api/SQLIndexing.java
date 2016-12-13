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
package org.apache.polygene.index.sql.support.api;

import java.sql.SQLException;
import org.apache.polygene.spi.entity.EntityState;

/**
 * This is the interface used by SQL-Indexing whenever
 */
public interface SQLIndexing
{
    /**
     * This method is called when states need to be indexed.
     * 
     * @param changedStates The states which changed.
     * @throws SQLException If SQL.
     */
    void indexEntities( Iterable<EntityState> changedStates )
        throws SQLException;

}
