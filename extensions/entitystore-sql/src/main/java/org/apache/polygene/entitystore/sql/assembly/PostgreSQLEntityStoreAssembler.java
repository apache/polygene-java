package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class PostgreSQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<PostgreSQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.POSTGRES;
    }

}
