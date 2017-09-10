package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class DerbySQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<DerbySQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.DERBY;
    }

}
