package org.qi4j.library.framework;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.annotation.scope.Entity;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.api.persistence.EntitySession;
import org.qi4j.api.query.QueryBuilderFactoryImpl;
import org.qi4j.api.query.QueryableIterable;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;
import org.qi4j.runtime.persistence.EntitySessionFactoryImpl;
import org.qi4j.runtime.resolution.EntityDependencyResolver;

public class FinderMixinTest
    extends TestCase
{
    public void testFinderMixin() throws Exception
    {
        CompositeBuilderFactoryImpl cbf = new CompositeBuilderFactoryImpl();

        EntitySession session = new EntitySessionFactoryImpl( cbf ).newEntitySession();
        EntityDependencyResolver entityDependencyResolver = new EntityDependencyResolver( session );

        List objects = new ArrayList();
        entityDependencyResolver.addQueryFactory( "someQuery", new QueryBuilderFactoryImpl( new QueryableIterable( objects ) ) );
        cbf.getDependencyResolverDelegator().setDependencyResolver( Entity.class, entityDependencyResolver );

        CompositeBuilder<Composite1> cb = cbf.newCompositeBuilder( FinderMixinTest.Composite1.class );
        FinderMixinTest.Test1 test = cb.newInstance();
        objects.add( test );
        assertEquals( "Foo", test.getFoo() );
    }

    @Mixins( Test1.Test1Mixin.class )
    public interface Test1
    {
        public String getName();

        public String getFoo();

        public class Test1Mixin
            implements FinderMixinTest.Test1
        {
            @ThisAs Finder finder;

            public String getName()
            {
                return "Foo";
            }

            public String getFoo()
            {
                Composite1 composite1 = finder.findByName( "Foo" );
                return composite1.getName();
            }

            public interface Finder
            {
                Composite1 findByName( String name );
            }
        }
    }

    @Mixins( FinderMixin.class )
    public interface Composite1 extends Composite, FinderMixinTest.Test1
    {
    }
}
