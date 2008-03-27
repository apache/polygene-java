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
import org.qi4j.runtime.query.model.CompanyComposite;
import org.qi4j.runtime.query.model.Nameable;

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
        QueryBuilder<CompanyComposite> queryBuilder = new QueryBuilderImpl<CompanyComposite>();
        Nameable nameable = templateFor( Nameable.class );
        queryBuilder.where(
            eq( nameable.name(), "JayWay" )
        );
    }

    /**
     * Query builder usage test.
     */
    @Test
    public void script02()
    {
        QueryBuilder<CompanyComposite> queryBuilder = new QueryBuilderImpl<CompanyComposite>();
        Nameable nameable = templateFor( Nameable.class );
        Company company = templateFor( Company.class );
        queryBuilder.where(
            and( eq( nameable.name(), "Jayway" ),
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
        QueryBuilder<CompanyComposite> queryBuilder = new QueryBuilderImpl<CompanyComposite>();
        Nameable nameable = templateFor( Nameable.class );
        Company company = templateFor( Company.class );
        queryBuilder.where(
            and( eq( nameable.name(), "Jayway" ),
                 isNotNull( company.stockQuote() )
            )
        );
    }

}