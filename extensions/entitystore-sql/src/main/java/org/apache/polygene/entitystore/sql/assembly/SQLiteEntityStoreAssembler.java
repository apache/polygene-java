package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class SQLiteEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<SQLiteEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.SQLITE;
    }

}
