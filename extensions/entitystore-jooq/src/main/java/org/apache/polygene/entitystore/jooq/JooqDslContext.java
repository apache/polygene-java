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
 */
package org.apache.polygene.entitystore.jooq;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

@Mixins( JooqDslContext.Mixin.class )
public interface JooqDslContext extends DSLContext
{

    class Mixin
        implements InvocationHandler
    {
        private DSLContext dsl;

        public Mixin( @Service DataSource dataSource, @Uses Settings settings, @Uses SQLDialect dialect )
        {
            Configuration configuration = new DefaultConfiguration()
                .set( dataSource )
                .set( dialect )
                .set( settings );
            dsl = DSL.using( configuration );
        }

        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            return method.invoke( dsl, objects );       // delegate all
        }
    }
}
