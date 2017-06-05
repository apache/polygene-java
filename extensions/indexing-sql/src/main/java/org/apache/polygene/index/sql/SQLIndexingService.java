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
package org.apache.polygene.index.sql;

import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.index.sql.internal.SQLEntityFinder;
import org.apache.polygene.index.sql.internal.SQLStateChangeListener;
import org.apache.polygene.spi.entitystore.StateChangeListener;
import org.apache.polygene.spi.query.EntityFinder;

/**
 * This is actual service responsible of managing indexing and queries and creating database structure.
 * <p>
 * The reason why all these components are in one single service is that they all require some data about
 * the database structure. Rather than exposing all of that data publicly to be available via another service,
 * it is stored in a state-style private mixin. Thus all the database-related data is available only to this
 * service, and no one else.
 * </p>
 */
@Mixins( {
    SQLEntityFinder.class,
    SQLStateChangeListener.class
} )
public interface SQLIndexingService
        extends StateChangeListener, EntityFinder
{
}
