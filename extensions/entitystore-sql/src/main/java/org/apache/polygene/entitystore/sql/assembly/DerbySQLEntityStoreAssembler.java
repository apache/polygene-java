package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

/**
 * Assembly for the Derby SQL Entity Store.
 */
public class DerbySQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<DerbySQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.DERBY;
    }

}
