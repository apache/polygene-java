package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Function;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FunctionalListTest extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( List.class ).withTypes( MapOp.class ).withMixins( ArrayList.class );
    }

    @Test
    public void givenArrayListWithMapOpCapabilityWhenMappingIntegerToStringExpectCorrectResult()
    {
        List<Integer> list = module.newTransient( List.class );
        list.add( 5 );
        list.add( 15 );
        list.add( 45 );
        list.add( 85 );
        MapOp<Integer> op = (MapOp<Integer>) list;

        List<String> strings = op.map( new Function<Integer, String>()
        {
            @Override
            public String map( Integer integer )
            {
                return integer.toString();
            }
        } );

        List<String> expected = new ArrayList<String>();
        expected.add( "5" );
        expected.add( "15" );
        expected.add( "45" );
        expected.add( "85" );
        assertThat( strings, equalTo( expected ) );
    }

    @Mixins( ListMapOpMixin.class )
    public interface MapOp<FROM>
    {
        <TO> List<TO> map( Function<FROM, TO> function );
    }

    public class ListMapOpMixin<FROM>
        implements MapOp<FROM>
    {
        @This
        private List<FROM> list;

        @Override
        public <TO> List<TO> map( Function<FROM, TO> function )
        {
            ArrayList<TO> result = new ArrayList<TO>();
            for( FROM data : list )
            {
                result.add( function.map( data ) );
            }
            return result;
        }
    }
}
