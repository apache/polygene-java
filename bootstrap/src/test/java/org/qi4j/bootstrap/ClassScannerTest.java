package org.qi4j.bootstrap;

import org.junit.Test;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.somepackage.Test2Value;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.bootstrap.ClassScanner.getClasses;
import static org.qi4j.bootstrap.ClassScanner.matches;

/**
 * Test and showcase of the ClassScanner assembly utility.
 *
 */
public class ClassScannerTest
{
    @Test
    public void testClassScanner()
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                // Find all classes starting from TestValue, but include only the ones that are named *Value

                for( Class aClass : filter( matches( ".*Value" ), getClasses( TestValue.class ) ))
                {
                    module.values(aClass);
                }
            }
        };

        singleton.valueBuilderFactory().newValueBuilder( TestValue.class );
        singleton.valueBuilderFactory().newValueBuilder( Test2Value.class );
    }


}
