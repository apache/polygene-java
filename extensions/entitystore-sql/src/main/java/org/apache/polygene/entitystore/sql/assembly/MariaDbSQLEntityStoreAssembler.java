package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

/**
 * Assembly for the MariaDb SQL Entity Store.
 */
public class MariaDbSQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<MariaDbSQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.MARIADB;
    }

}
