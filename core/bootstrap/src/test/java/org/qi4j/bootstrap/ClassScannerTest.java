package org.qi4j.bootstrap;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.bootstrap.somepackage.Test2Value;
import org.qi4j.functional.Iterables;

import static org.qi4j.bootstrap.ClassScanner.findClasses;
import static org.qi4j.bootstrap.ClassScanner.matches;
import static org.qi4j.functional.Iterables.filter;

/**
 * Test and showcase of the ClassScanner assembly utility.
 */
public class ClassScannerTest
{
    @Test
    public void testClassScannerFiles()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // Find all classes starting from TestValue, but include only the ones that are named *Value

                for( Class aClass : filter( matches( ".*Value" ), findClasses( TestValue.class ) ) )
                {
                    module.values( aClass );
                }
            }
        };

        singleton.module().newValueBuilder( TestValue.class );
        singleton.module().newValueBuilder( Test2Value.class );
    }

    @Test
    public void testClassScannerJar()
    {
        Assert.assertEquals( 138, Iterables.count( findClasses( Test.class ) ) );
    }
}
