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
package org.qi4j.runtime.query;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

public class NonQueryableTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Test
    public void whenQuerableIsFalseOnPropertyThenExpectException()
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            QueryBuilder<Abc> builder = module.newQueryBuilder( Abc.class );
            Abc proto = templateFor( Abc.class );
            builder.where( eq( proto.isValid(), Boolean.TRUE ) );
            Assert.fail( "Exception was expected." );
        }
        catch( QueryException e )
        {
            // expected!!
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void testQueryIterable()
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            module.newQueryBuilder( Abc2.class );
            Assert.fail( "Exception was expected." );
        }
        catch( QueryException e )
        {
            // expected!!
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    static interface Abc
    {
        @Queryable( false )
        Property<Boolean> isValid();
    }

    @Queryable( false )
    public interface Abc2
    {
        Property<Boolean> isValid();
    }
}
