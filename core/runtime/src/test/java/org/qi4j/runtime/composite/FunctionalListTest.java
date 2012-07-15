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
        module.transients( List.class ).withTypes( FList.class ).withMixins( ArrayList.class );
    }

    @Test
    public void givenArrayListWithMapOpCapabilityWhenMappingIntegerToStringExpectCorrectResult()
    {
        List<Integer> integers = module.newTransient( List.class );
        integers.add( 5 );
        integers.add( 15 );
        integers.add( 45 );
        integers.add( 85 );
        FList<Integer> list = (FList<Integer>) integers;

        List<String> strings = list.translate( new Function<Integer, String>()
        {
            @Override
            public String map( Integer x )
            {
                return x.toString();
            }
        } );

        List<String> expected = new ArrayList<String>();
        expected.add( "5" );
        expected.add( "15" );
        expected.add( "45" );
        expected.add( "85" );
        assertThat( strings, equalTo( expected ) );
    }

    @Mixins( FListMixin.class )
    public interface FList<FROM>
    {
        <TO> List<TO> translate( Function<FROM, TO> function );
    }

    public class FListMixin<FROM>
        implements FList<FROM>
    {
        @This
        private List<FROM> list;

        @Override
        public <TO> List<TO> translate( Function<FROM, TO> function )
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
