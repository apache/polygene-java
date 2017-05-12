package org.apache.polygene.entitystore.jooq;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceDescriptor;
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

        public Mixin( @Service DataSource dataSource, @Uses ServiceDescriptor serviceDescriptor )
        {
            Settings settings = serviceDescriptor.metaInfo( Settings.class );
            SQLDialect sqlDialect = serviceDescriptor.metaInfo( SQLDialect.class );
            Configuration configuration = new DefaultConfiguration()
                .set( dataSource )
                .set( sqlDialect )
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
