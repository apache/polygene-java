package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

/**
 * Assembly for the Postgresql SQL Entity Store.
 */
public class PostgreSQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<PostgreSQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.POSTGRES;
    }

}
