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

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.spi.query.EntityFinderException;

/**
 * This interface provides parsing support from Polygene queries to SQL queries. The idea is that this interface produces
 * SQL which can be used when creating a {@link PreparedStatement}.
 *
 * @author Stanislav Muhametsin
 */
public interface SQLQuerying
{
    /**
     * This method will be called when parsing needs to be done from Polygene query to SQL query. This method is supposed to
     * return one single SQL query, which can be used with {@link PreparedStatement}.
     *
     * @param resultType The result type of Polygene query.
     * @param whereClause The where clause of Polygene query.
     * @param orderBySegments The order by segments of Polygene query.
     * @param firstResult The first result index of Polygene query.
     * @param maxResults The max amount of returned results.
     * @param values Values to put into {@link PreparedStatement}, in the order of the list. This List is created from
     *            the outside and this method is supposed to fill it with necessary values, if any.
     * @param valueSqlTypes The SQL types of the objects in {@code values}. Each SQL type at index {@code x} is
     *            interpreted as type of value at index {@code x} of the {@code values} -list.
     * @param countOnly True if this query should return only the number of matching entities, false otherwise.
     * @return The SQL query, which may be used with {@link PreparedStatement}.
     * @throws EntityFinderException If SQLException or something else bad happens.
     */
    String constructQuery( //
        Class<?> resultType, //
        @Optional Predicate<Composite> whereClause, //
        @Optional List<OrderBy> orderBySegments, //
        @Optional Integer firstResult, //
        @Optional Integer maxResults, //
        Map<String, Object> variables,
        List<Object> values, //
        List<Integer> valueSqlTypes, //
        Boolean countOnly //
    )
        throws EntityFinderException;

    Boolean isFirstResultSettingSupported();

    Integer getResultSetType( //
        @Optional Integer firstResult, //
        @Optional Integer maxResults //
    );
}
