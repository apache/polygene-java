package org.qi4j.entity.index.rdf;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.qi4j.association.Association;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffects;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

public class SesameIndexerTest
{
    @Test
    public void script01() throws UnitOfWorkCompletionException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites(
                    PersonComposite.class,
                    CityComposite.class
                );
                module.addServices(
                    IndexableEntityStoreComposite.class,
                    UuidIdentityGeneratorComposite.class,
                    RDFIndexerComposite.class
                );
            }
        };
        UnitOfWork unitOfWork = assembler.getUnitOfWorkFactory().newUnitOfWork();

        CompositeBuilder<CityComposite> cityBuilder = unitOfWork.newEntityBuilder( CityComposite.class );
        City kualaLumpur = cityBuilder.newInstance();
        kualaLumpur.name().set( "Kuala Lumpur" );

        CompositeBuilder<PersonComposite> personBuilder = unitOfWork.newEntityBuilder( PersonComposite.class );

        Person annDoe = personBuilder.newInstance();
        annDoe.name().set( "Ann Doe" );
        annDoe.placeOfBirth().set( kualaLumpur );

        Person joeDoe = personBuilder.newInstance();
        joeDoe.name().set( "Joe Doe" );
        joeDoe.placeOfBirth().set( kualaLumpur );
        joeDoe.mother().set( annDoe );

        unitOfWork.complete();
    }

    public static interface Person
    {
        Property<String> name();
        Association<City> placeOfBirth();
        Association<Person> mother();
    }

    public static interface City
    {
        Property<String> name();
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public static interface PersonComposite
        extends Person, EntityComposite
    {
    }

    @Mixins( PropertyMixin.class )
    public static interface CityComposite
        extends City, EntityComposite
    {
    }

    @SideEffects( IndexingSideEffect.class )
    public static interface IndexableEntityStoreComposite
        extends MemoryEntityStoreComposite
    {

    }

    public abstract static class IndexingSideEffect
        implements SerializationStore
    {

        @SideEffectFor SerializationStore serializationStore;
        @Service Indexer indexer;

        public StateCommitter prepare( Map<SerializedEntity, SerializedState> newEntities,
                                       Map<SerializedEntity, SerializedState> updatedEntities,
                                       Iterable<SerializedEntity> removedEntities ) throws IOException
        {
            indexer.index( newEntities, updatedEntities, removedEntities );
            indexer.toRDF( System.out );
            return null;
        }
    }

}
