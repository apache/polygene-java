/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.sql;

import java.util.List;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.test.indexing.AbstractNamedQueryTest;

@Ignore
public class SqlNamedQueryTest
    extends AbstractNamedQueryTest
{
    @Override
    protected String[] queryStrings()
    {
        return queryStrings;
    }

    @Override
    protected NamedQueryDescriptor createNamedQueryDescriptor( String queryName, String queryString )
    {
        NamedSqlDescriptor descriptor = new NamedSqlDescriptor( queryName, queryString, null );
        return descriptor;
    }

    @Override
    protected void setupTest( ModuleAssembly module, NamedQueries namedQueries )
        throws AssemblyException
    {
    }

    @Override
    protected void tearDownTest()
    {
    }

    private static String[] queryStrings =
        {
            "", //script01

            "", //script02

            "", //script03

            "", //script04

            "", //script05

            "", //script06

            "", //script07

            "", //script08

            "", //script09

            "", //script10

            "", //script11

            "", //script12

            "", //script13

            "", //script14

            "", //script15

            "", //script16

            "", //script17

            "", //script18

            "", //script19

            "", //script20

            "", //script21

            "", //script22

            "", //script23

            "", //script24

            "", //script25
            "", //script26
            "", //script27
            "", //script28
            "", //script29
            "", //script30
            ""  //script31
        };
}