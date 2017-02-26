package org.apache.polygene.serialization.javaxxml;

import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.entity.AbstractConfigurationDeserializationTest;
import org.junit.Test;

public class JavaxXmlConfigurationDeserializationTest extends AbstractConfigurationDeserializationTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxXmlSerializationAssembler().assemble( module );
        super.assemble( module );
    }

    @Test
    public void givenServiceWhenInitializingExpectCorrectDeserialization()
    {
        super.givenServiceWhenInitializingExpectCorrectDeserialization();
    }
}
