/*
 * Copyright 2008 Alin Dreghiciu.
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
 *
 */
package org.qi4j.runtime.query;

import org.junit.Test;
import org.qi4j.query.QueryBuilder;
import static org.qi4j.query.QueryExpressions.*;
import org.qi4j.runtime.query.model.Company;
import org.qi4j.runtime.query.model.Employee;
import org.qi4j.runtime.query.model.HasName;
import org.qi4j.runtime.query.model.Person;

/**
 * Unit tests for {@link QueryBuilderImpl}.
 */
public class QueryBuilderImplTest
{

    /**
     * Query builder usage test.
     */
    @Test
    public void script01()
    {
        QueryBuilder<Company> queryBuilder = new QueryBuilderImpl<Company>();
        HasName hasName = templateFor( HasName.class );
        queryBuilder.where(
            eq( hasName.name(), "JayWay" )
        );
    }

    /**
     * Query builder usage test.
     */
    @Test
    public void script02()
    {
        QueryBuilder<Company> queryBuilder = new QueryBuilderImpl<Company>();
        HasName hasName = templateFor( HasName.class );
        Company company = templateFor( Company.class );
        queryBuilder.where(
            and( eq( hasName.name(), "Jayway" ),
                 or( eq( company.stockQuote(), "JAWY" ),
                     eq( company.stockQuote(), "JAY" )
                 )
            )
        );
    }

    /**
     * Query builder usage test.
     */
    @Test
    public void script03()
    {
        QueryBuilder<Company> queryBuilder = new QueryBuilderImpl<Company>();
        HasName hasName = templateFor( HasName.class );
        Company company = templateFor( Company.class );
        queryBuilder.where(
            and( eq( hasName.name(), "Jayway" ),
                 isNotNull( company.stockQuote() )
            )
        );
    }

    /**
     * Query builder usage test.
     */
    @Test
    public void script04()
    {
        QueryBuilder<Employee> queryBuilder = new QueryBuilderImpl<Employee>();
        Employee employee = templateFor( Employee.class );
        queryBuilder.where(
            eq( employee.employer().get().name(), "Jaway" )
        );
    }

    /**
     * Query builder usage test.
     */
    @Test
    public void script05()
    {
        QueryBuilder<Employee> queryBuilder = new QueryBuilderImpl<Employee>();
        Employee employee = templateFor( Employee.class );
        queryBuilder.where(
            eq( employee.employer().get().businessAddress().get().city().get().name(), "Kuala Lumpur" )
        );
    }

}