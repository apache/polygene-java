package org.qi4j.api.dataset.iterable;

import org.junit.Before;
import org.qi4j.api.dataset.DataSet;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Iterables;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class IterableDataSetTest
    extends AbstractQi4jTest
{
    DataSet<TestValue> dataSet;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( TestValue.class );
    }

    @Before
    public void setUp()
    {
        dataSet = new IterableDataSet<TestValue>( Iterables.iterable( newTestValue( "Rickard" ), newTestValue( "Niclas" ), newTestValue( "Paul" ) ) );
    }

    private TestValue newTestValue( String name )
    {
        return module.newValueFromSerializedState( TestValue.class, "{name:'" + name + "'}" );
    }

    interface TestValue
    {
        Property<String> name();
    }
}
