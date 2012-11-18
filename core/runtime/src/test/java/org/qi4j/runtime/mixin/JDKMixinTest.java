package org.qi4j.runtime.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.qi4j.functional.Iterables.*;

/**
 * Assert that JDK classes are usable as Mixins.
 */
public class JDKMixinTest
    extends AbstractQi4jTest
{

    public interface JSONSerializableMap
        extends Map<String, String>
    {

        JSONObject toJSON();

    }

    public static class ExtendsJDKMixin
        extends HashMap<String, String>
        implements JSONSerializableMap
    {

        @Override
        public JSONObject toJSON()
        {
            System.out.println( ">>>> Call ExtendsJDKMixin.toJSON()" );
            return new JSONObject( this );
        }

    }

    public static abstract class ComposeWithJDKMixin
        implements JSONSerializableMap
    {

        @This
        private Map<String, String> map;

        @Override
        public JSONObject toJSON()
        {
            System.out.println( ">>>> Call ComposeWithJDKMixin.toJSON()" );
            return new JSONObject( map );
        }

    }

    private static final String EXTENDS_IDENTITY = ExtendsJDKMixin.class.getName();
    private static final String COMPOSE_IDENTITY = ComposeWithJDKMixin.class.getName();
    private static final Specification<ServiceReference> EXTENDS_IDENTITY_SPEC = new ServiceIdentitySpec( EXTENDS_IDENTITY );
    private static final Specification<ServiceReference> COMPOSE_IDENTITY_SPEC = new ServiceIdentitySpec( COMPOSE_IDENTITY );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( JSONSerializableMap.class ).
            identifiedBy( EXTENDS_IDENTITY ).
            withMixins( ExtendsJDKMixin.class ).
            instantiateOnStartup();

        module.layer().module( "compose" ).services( JSONSerializableMap.class ).
            visibleIn( Visibility.layer ).
            identifiedBy( COMPOSE_IDENTITY ).
            withMixins( HashMap.class, ComposeWithJDKMixin.class ).
            instantiateOnStartup();
    }

    @Test
    public void testMixinExtendsJDK()
    {
        List<ServiceReference<JSONSerializableMap>> services = toList(
            filter( EXTENDS_IDENTITY_SPEC,
                    module.findServices( JSONSerializableMap.class ) ) );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( EXTENDS_IDENTITY ) );

        JSONSerializableMap extending = services.get( 0 ).get();
        extending.put( "foo", "bar" );
        JSONObject json = extending.toJSON();

        assertThat( json.length(), equalTo( 1 ) );
        assertThat( json.optString( "foo" ), equalTo( "bar" ) );
    }

    @Test
    public void testComposeJDKMixin()
    {
        List<ServiceReference<JSONSerializableMap>> services = toList(
            filter( COMPOSE_IDENTITY_SPEC,
                    module.findServices( JSONSerializableMap.class ) ) );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( COMPOSE_IDENTITY ) );

        JSONSerializableMap composing = services.get( 0 ).get();
        composing.put( "foo", "bar" );
        JSONObject json = composing.toJSON();

        assertThat( json.length(), equalTo( 1 ) );
        assertThat( json.optString( "foo" ), equalTo( "bar" ) );
    }

    private static class ServiceIdentitySpec
        implements Specification<ServiceReference>
    {

        private final String identity;

        public ServiceIdentitySpec( String identity )
        {
            this.identity = identity;
        }

        @Override
        public boolean satisfiedBy( ServiceReference item )
        {
            return item.identity().equals( identity );
        }

    }

}