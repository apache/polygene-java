/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.runtime.query;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

public class NonQueryableTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Test
    public void whenQuerableIsFalseOnPropertyThenExpectException()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            QueryBuilder<Abc> builder = queryBuilderFactory.newQueryBuilder( Abc.class );
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
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            queryBuilderFactory.newQueryBuilder( Abc2.class );
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
