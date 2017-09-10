package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class MariaDbSQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<MariaDbSQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.MARIADB;
    }

}
