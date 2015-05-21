package org.qi4j.runtime.value;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

public class ValueSerializationRegressionTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( SimpleEntity.class );
        module.entities( DualFaced.class );
        module.values( DualFaced.class );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
    }

    @Test
    public void givenNewValueWhenConvertingToEntityExpectNewEntityInStore()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<DualFaced> builder = module.newValueBuilder( DualFaced.class );
        builder.prototype().identity().set( "1234" );
        builder.prototype().name().set( "Hedhman" );
        DualFaced value = builder.newInstance();
    }

    public interface SimpleEntity extends Identity
    {
        Property<String> name();
    }

    public interface DualFaced extends Identity
    {
        Property<String> name();

        Association<SimpleEntity> simple();

        ManyAssociation<SimpleEntity> simples();

        NamedAssociation<SimpleEntity> namedSimples();
    }
}
