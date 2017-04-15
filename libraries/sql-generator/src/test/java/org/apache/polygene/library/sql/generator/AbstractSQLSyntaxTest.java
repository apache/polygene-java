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

import org.apache.polygene.library.sql.generator.grammar.common.SQLStatement;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Muhametsin
 */
public abstract class AbstractSQLSyntaxTest
{

    private SQLVendor _vendor;

    protected void logStatement( String statementType, SQLVendor vendor, SQLStatement statement )
    {
        String stringStmt = vendor.toString( statement );
        LoggerFactory.getLogger( this.getClass().getName() ).info( statementType + ":" + "\n" + stringStmt + "\n" );

        Assert.assertEquals(
            "Strings must be same from both SQLVendor.toString(...) and statement.toString() methods.", stringStmt,
            statement.toString() );
    }

    @Before
    public final void setUp()
        throws Exception
    {
        this._vendor = this.loadVendor();
    }

    @After
    public final void tearDown()
    {
        this._vendor = null;
    }

    protected final SQLVendor getVendor()
    {
        return this._vendor;
    }

    protected abstract SQLVendor loadVendor()
        throws Exception;
}
