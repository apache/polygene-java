package com.marcgrue.dcisample_a.infrastructure.model;

import java.io.Serializable;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Queries base class
 */
public class Queries
    implements Serializable // For Wicket (don't remove)
{
    static protected UnitOfWorkFactory uowf;
    static protected QueryBuilderFactory qbf;

    public static void prepareQueriesBaseClass( UnitOfWorkFactory unitOfWorkFactory,
                                                QueryBuilderFactory queryBuilderFactory
    )
    {
        uowf = unitOfWorkFactory;
        qbf = queryBuilderFactory;
    }
}