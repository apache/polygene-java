package org.qi4j.api.configuration;

import org.junit.Test;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConfigurationTest extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MyService.class ).instantiateOnStartup();
        module.entities( MyConfig.class );
        module.values( PersonDetails.class, Address.class, City.class, Country.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testConfiguration()
        throws Exception
    {
        MyService service = module.findService( MyService.class ).get();
        PersonDetails details = service.details();
        assertThat(details.name().get(), equalTo( "Niclas" ) );
        assertThat(details.address().get().street1().get(), equalTo( "Henan Lu 555" ) );
        assertThat(details.address().get().street2().get(), equalTo( "Block 15" ) );
        assertThat(details.address().get().city().get().cityName().get(), equalTo( "Shanghai" ) );
        assertThat(details.address().get().city().get().country().get().countryName().get(), equalTo( "China" ) );
    }

    @Mixins(MyServiceMixin.class)
    public interface MyService extends ServiceComposite
    {
        PersonDetails details();
    }

    public abstract class MyServiceMixin
        implements MyService
    {
        @This
        Configuration<MyConfig> myconf;

        @Override
        public PersonDetails details()
        {
            return myconf.get().me().get();
        }
    }

    public interface MyConfig extends ConfigurationComposite
    {
        Property<PersonDetails> me();
    }

    public interface PersonDetails extends ValueComposite
    {
        Property<String> name();
        Property<Address> address();
    }

    public interface Address extends ValueComposite
    {
        Property<String> street1();
        Property<String> street2();
        Property<City> city();
    }

    public interface City extends ValueComposite
    {
        Property<String> cityName();
        Property<Country> country();
    }

    public interface Country extends ValueComposite
    {
        Property<String> countryName();
    }
}
