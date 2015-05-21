package org.qi4j.runtime.value;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ValueWithAssociationTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( SimpleName.class );
        module.entities( DualFaced.class );
        module.values( DualFaced.class );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
    }

    @Test
    public void givenEntityInStoreWhenFetchEntityReferenceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        String identity1;
        String identity2;
        DualFaced value;
        try (UnitOfWork uow = module.newUnitOfWork())
        {
            EntityBuilder<SimpleName> builder1 = uow.newEntityBuilder( SimpleName.class );
            builder1.instance().name().set( "Niclas" );
            SimpleName simpleEntity = builder1.newInstance();
            identity1 = simpleEntity.identity().get();

            EntityBuilder<DualFaced> builder2 = uow.newEntityBuilder( DualFaced.class );
            DualFaced proto = builder2.instance();
            proto.name().set( "Hedhman" );
            proto.simple().set( simpleEntity );
            proto.simples().add( simpleEntity );
            proto.namedSimples().put( "niclas", simpleEntity );
            DualFaced faced = builder2.newInstance();
            identity2 = faced.identity().get();
            value = spi.toValue( DualFaced.class, faced );
            assertThat( value.identity().get(), equalTo( identity2 ) );
            uow.complete();
        }

        try (UnitOfWork uow = module.newUnitOfWork())
        {
            DualFaced entity = uow.get( DualFaced.class, identity2 );
            AssociationStateHolder holder = spi.stateOf( (EntityComposite) entity );
            Association<?> simple = holder.allAssociations().iterator().next();
            ManyAssociation<?> simples = holder.allManyAssociations().iterator().next();
            NamedAssociation<?> namedSimples = holder.allNamedAssociations().iterator().next();

            assertThat( spi.entityReferenceOf( simple ), equalTo( EntityReference.parseEntityReference( identity1 ) ) );
            assertThat( spi.entityReferenceOf( simples )
                            .iterator()
                            .next(), equalTo( EntityReference.parseEntityReference( identity1 ) ) );
            assertThat( spi.entityReferenceOf( namedSimples )
                            .iterator()
                            .next()
                            .getValue(), equalTo( EntityReference.parseEntityReference( identity1 ) ) );

            DualFaced resurrected = spi.toEntity( DualFaced.class, value );
            assertThat( resurrected.simple(), equalTo( entity.simple() ) );
            assertThat( resurrected.simples(), equalTo( entity.simples() ) );
            assertThat( resurrected.namedSimples(), equalTo( entity.namedSimples() ) );
        }
    }

    @Test
    public void givenNewValueWhenConvertingToEntityExpectNewEntityInStore()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<DualFaced> builder = module.newValueBuilder( DualFaced.class );
        builder.prototype().identity().set( "1234" );
        builder.prototype().name().set( "Hedhman" );
        DualFaced value = builder.newInstance();

        try (UnitOfWork uow = module.newUnitOfWork())
        {
            spi.toEntity( DualFaced.class, value );
            uow.complete();
        }

        try (UnitOfWork uow = module.newUnitOfWork())
        {
            DualFaced entity = uow.get( DualFaced.class, "1234" );
            assertThat( entity.identity().get(), equalTo( "1234" ) );
            assertThat( entity.name().get(), equalTo( "Hedhman" ) );
            uow.complete();
        }
    }

    public interface SimpleName extends Identity
    {
        Property<String> name();
    }

    public interface DualFaced extends Identity
    {
        Property<String> name();

        @Optional
        Association<SimpleName> simple();

        ManyAssociation<SimpleName> simples();

        NamedAssociation<SimpleName> namedSimples();
    }
}
