package org.apache.polygene.entitystore.sql.assembly;

import org.jooq.SQLDialect;

public class H2SQLEntityStoreAssembler extends AbstractSQLEntityStoreAssembler<H2SQLEntityStoreAssembler>
{
    protected SQLDialect getSQLDialect()
    {
        return SQLDialect.H2;
    }

}
