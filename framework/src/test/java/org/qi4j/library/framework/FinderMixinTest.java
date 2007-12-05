package org.qi4j.library.framework;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.annotation.Mixins;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntitySession;
import org.qi4j.query.QueryBuilderFactoryImpl;
import org.qi4j.query.QueryableIterable;
import org.qi4j.runtime.injection.EntityInjectionProviderFactory;
import org.qi4j.runtime.persistence.EntitySessionFactoryImpl;
import org.qi4j.test.AbstractQi4jTest;

public class FinderMixinTest
    extends AbstractQi4jTest
{
    @Override public void configure( ModuleAssembly module )
    {
        module.addComposite( Composite1.class );
    }

    public void testFinderMixin() throws Exception
    {
        EntitySession session = new EntitySessionFactoryImpl( compositeBuilderFactory ).newEntitySession();
        EntityInjectionProviderFactory entityInjectionResolver = new EntityInjectionProviderFactory( session );

        List objects = new ArrayList();
        entityInjectionResolver.addQueryFactory( "someQuery", new QueryBuilderFactoryImpl( new QueryableIterable( objects ) ) );

        CompositeBuilder<Composite1> cb = compositeBuilderFactory.newCompositeBuilder( FinderMixinTest.Composite1.class );
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
            @ThisCompositeAs Finder finder;

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
