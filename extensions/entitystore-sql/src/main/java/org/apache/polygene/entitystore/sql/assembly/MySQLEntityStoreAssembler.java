package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class MySQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<MySQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.MYSQL;
    }

}
