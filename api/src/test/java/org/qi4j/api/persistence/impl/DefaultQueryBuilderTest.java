package org.qi4j.api.persistence.impl;
/**
 *  TODO
 */

import org.qi4j.api.model.Mixin1;
import org.qi4j.api.query.QueryTest;
import org.qi4j.api.query.decorator.DefaultQueryFactory;

public class DefaultQueryBuilderTest extends QueryTest
{
    DefaultQueryFactory defaultQueryFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        defaultQueryFactory = new DefaultQueryFactory( createObjects() );
        query = defaultQueryFactory.newQuery( Mixin1.class );
    }
}