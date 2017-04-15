/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.polygene.library.sql.generator;

import org.apache.polygene.library.sql.generator.vendor.MySQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendorProvider;
import org.junit.Test;

/**
 * @author Stanislav Muhametsin
 */
public class MySQLQueryTest extends AbstractQueryTest
{

    @Override
    protected SQLVendor loadVendor()
        throws Exception
    {
        return SQLVendorProvider.createVendor( MySQLVendor.class );
    }

    @Test
    public void mySQLQuery5()
        throws Exception
    {
        ( (MySQLVendor) this.getVendor() ).setLegacyLimit( true );
        super.query5();
    }

    @Test
    public void mySQLQuery6()
        throws Exception
    {
        ( (MySQLVendor) this.getVendor() ).setLegacyLimit( true );
        super.query6();
    }

    @Test
    public void mySQLQuery7()
        throws Exception
    {
        ( (MySQLVendor) this.getVendor() ).setLegacyLimit( true );
        super.query7();
    }
}
