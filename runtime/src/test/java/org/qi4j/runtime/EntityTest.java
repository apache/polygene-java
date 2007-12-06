package org.qi4j.runtime;

import java.util.List;
import junit.framework.TestCase;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Entity;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.PropertyField;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;

/**
 * TODO
 */
public class EntityTest
    extends TestCase
{
    public void testEntity()
    {
/*
        ModuleCompositeBuilderFactory cbf = new ModuleCompositeBuilderFactory();

        List results = new ArrayList();
        for( int i = 0; i < 10; i++ )
        {
            CompositeBuilder<Composite2> cb = cbf.newCompositeBuilder( Composite2.class );
            cb.properties().setBar( i );
            cb.properties().setFoo( "Foo " + i );
            results.add( cb.newInstance() );
        }

        EntitySession session = new EntitySessionFactoryImpl( cbf ).newEntitySession();
        EntityInjectionProviderFactory entityDependencyResolver = new EntityInjectionProviderFactory( session );

//        entityDependencyResolver.addQueryFactory( "someQuery", new DefaultQueryFactory( results ) );
        cbf.getDependencyResolverDelegator().setDependencyResolver( Entity.class, entityDependencyResolver );

        CompositeBuilder<AbcComposite> cb = cbf.newCompositeBuilder( AbcComposite.class );
        AbcComposite composite = cb.newInstance();
*/

        // TODO These don't work right now
//        checkEquals( composite.testFactory(), results );

//        checkEquals( composite.testQuery(), results );

//        checkEquals( composite.testIterable(), results );
    }

    private void checkEquals( Iterable<Test2> iter, List results )
    {
        int i = 0;
        for( Test2 test2 : iter )
        {
            Object result = results.get( i++ );
            assertEquals( result, test2 );
        }
    }


    interface Composite1 extends Composite, Test1
    {
    }

    @Mixins( Test1Mixin.class )
    interface Test1
    {
        public Test2 getFooInstance();

        public Iterable<Test2> testFactory();

        public Iterable<Test2> testQuery();

        public Iterable<Test2> testIterable();
    }

    public static class Test1Mixin
        implements Test1
    {
        @Entity( "foo" ) Test2 fooInstance;

        @Entity( "someQuery" ) QueryBuilderFactory someBuilderFactory;

        @Entity( "someQuery" ) Query<Test2> someQuery;

        @Entity( "someQuery" ) Iterable<Test2> someIterable;

        public Test2 getFooInstance()
        {
            return fooInstance;
        }

        public Iterable<Test2> testFactory()
        {
//            return someBuilderFactory.newQueryBuilder( Test2.class ).prepare();
            return null;
        }

        public Iterable<Test2> testQuery()
        {
//            return someQuery.prepare();
            return null;
        }

        public Iterable<Test2> testIterable()
        {
            return someIterable;
        }

    }

    interface Composite2 extends Composite, Test2
    {
    }


    @Mixins( Test2Mixin.class )
    interface Test2
    {
        String getFoo();

        void setFoo( String foo );

        int getBar();

        void setBar( int bar );
    }

    public static class Test2Mixin
        implements Test2
    {
        @PropertyField String foo;
        @PropertyField int bar;

        public String getFoo()
        {
            return foo;
        }

        public void setFoo( String foo )
        {
            this.foo = foo;
        }

        public int getBar()
        {
            return bar;
        }

        public void setBar( int bar )
        {
            this.bar = bar;
        }
    }
}
