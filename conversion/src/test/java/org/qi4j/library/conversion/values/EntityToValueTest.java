package org.qi4j.library.conversion.values;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class EntityToValueTest extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( EntityToValueService.class );
        module.services( MemoryEntityStoreService.class );
        module.entities( PersonEntity.class );
        module.values( PersonValue.class );
        module.values( PersonValue2.class );
        module.values( PersonValue3.class );
        module.values( PersonValue4.class );
    }

    @Test
    public void whenConvertingEntityToValueExpectCorrectValues()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceLocator.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();
            PersonValue niclasValue = service.convert( PersonValue.class, niclas );
            assertEquals( "Niclas", niclasValue.firstName().get() );
            assertEquals( "Hedhman", niclasValue.lastName().get() );
            assertEquals( "urn:qi4j:entity:id:Lis", niclasValue.spouse().get() );
            assertEquals( "urn:qi4j:entity:id:Eric", niclasValue.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenUnqualifiedValueWhenConvertingEntityExpectCorrectMapping()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceLocator.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue2 niclasValue = service.convert( PersonValue2.class, niclas );
            assertEquals( "Niclas", niclasValue.firstName().get() );
            assertEquals( "Hedhman", niclasValue.lastName().get() );
            assertEquals( "urn:qi4j:entity:id:Lis", niclasValue.spouse().get() );
            assertEquals( "urn:qi4j:entity:id:Eric", niclasValue.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenUnqualifiedValue2WhenConvertingEntityExpectCorrectMapping()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceLocator.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue3 niclasValue = service.convert( PersonValue3.class, niclas );
            assertEquals( "Niclas", niclasValue.firstName().get() );
            assertEquals( "Hedhman", niclasValue.lastName().get() );
            assertEquals( "urn:qi4j:entity:id:Lis", niclasValue.spouse().get() );
            assertEquals( "urn:qi4j:entity:id:Eric", niclasValue.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void givenQualifiedValueNotFromSameInterfaceWhenConvertingEntityExpectNonOptionalException()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceLocator.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue4 niclasValue = service.convert( PersonValue4.class, niclas );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    private PersonEntity setupPersonEntities( UnitOfWork uow )
    {
        PersonEntity niclas = createNiclas( uow );
        PersonEntity lis = createLis( uow );
        PersonEntity eric = createEric( uow );
        niclas.spouse().set( lis );
        niclas.children().add(eric);
        lis.spouse().set( niclas );
        lis.children().add(eric);
        assertEquals( "Niclas", niclas.firstName() );
        assertEquals( "Hedhman", niclas.lastName() );
        assertEquals( "Lis", lis.firstName() );
        assertEquals( "Gazi", lis.lastName() );
        assertEquals( "Eric", eric.firstName() );
        assertEquals( "Hedman", eric.lastName() );
        return niclas;
    }

    private PersonEntity createNiclas( UnitOfWork uow )
    {
        String firstName = "Niclas";
        String lastName = "Hedhman";
        Date birthTime = createBirthDate( 1964, 9, 25 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createLis( UnitOfWork uow )
    {
        String firstName = "Lis";
        String lastName = "Gazi";
        Date birthTime = createBirthDate( 1976, 2, 19 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createEric( UnitOfWork uow )
    {
        String firstName = "Eric";
        String lastName = "Hedman";
        Date birthTime = createBirthDate( 2004, 4, 8 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createPerson( UnitOfWork uow, String firstName, String lastName, Date birthTime )
    {
        EntityBuilder<PersonEntity> builder = uow.newEntityBuilder( PersonEntity.class, "id:" + firstName );
        PersonState state = builder.instanceFor( PersonState.class );
        state.firstName().set( firstName );
        state.lastName().set( lastName );
        state.dateOfBirth().set( birthTime );
        return builder.newInstance();
    }

    private Date createBirthDate( int year, int month, int day )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        calendar.set( year, month - 1, day, 12, 0, 0 );
        return calendar.getTime();
    }

    public interface PersonState
    {
        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();
    }

    public interface PersonValue extends PersonState, ValueComposite
    {
        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();
    }

    @Mixins( PersonMixin.class )
    public interface PersonEntity extends EntityComposite
    {
        String firstName();

        String lastName();

        Integer age();

        @Optional
        Association<PersonEntity> spouse();

        ManyAssociation<PersonEntity> children();
    }

    public static abstract class PersonMixin
        implements PersonEntity
    {
        @This
        private PersonState state;

        public String firstName()
        {
            return state.firstName().get();
        }

        public String lastName()
        {
            return state.lastName().get();
        }

        public Integer age()
        {
            long now = System.currentTimeMillis();
            long birthdate = state.dateOfBirth().get().getTime();
            return (int) ( ( now - birthdate ) / 1000 / 3600 / 24 / 365.25 );
        }
    }

    @Unqualified
    public interface PersonValue2 extends ValueComposite
    {
        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();
    }


    @Unqualified(true)
    public interface PersonValue3 extends ValueComposite
    {
        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();
    }

    @Unqualified(false)
    public interface PersonValue4 extends ValueComposite
    {
        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();
    }
}
