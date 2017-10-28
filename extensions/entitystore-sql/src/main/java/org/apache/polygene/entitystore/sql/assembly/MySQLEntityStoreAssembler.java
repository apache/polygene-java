package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

/**
 * Assembly for the MySQL Entity Store.
 */
public class MySQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<MySQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.MYSQL;
    }

}
