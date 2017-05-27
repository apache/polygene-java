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
package org.apache.polygene.runtime.query;

import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.entity.Queryable;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;

public class NonQueryableTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.defaultServices();
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
