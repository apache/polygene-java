package org.qi4j.valueserialization.jackson;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.value.AbstractJsonDateFormatTest;

public class JacksonJsonDateFormatTest
    extends AbstractJsonDateFormatTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.services( JacksonValueSerializationService.class );
    }
}
