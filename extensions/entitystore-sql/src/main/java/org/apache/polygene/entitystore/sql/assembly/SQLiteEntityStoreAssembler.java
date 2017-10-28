package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

/**
 * Assembly for the SQLite SQL Entity Store.
 */
public class SQLiteEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<SQLiteEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.SQLITE;
    }

}
