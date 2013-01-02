package org.qi4j.valueserialization.orgjson;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.value.AbstractJsonDateFormatTest;

public class OrgJsonDateFormatTest
    extends AbstractJsonDateFormatTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.services( OrgJsonValueSerializationService.class );
    }
}
