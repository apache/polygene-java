package org.apache.polygene.test.junit5;

import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.test.PolygeneUnitExtension;
import org.apache.polygene.test.model.Cat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * This test is to help develop the JUnit 5 Extension.
 */
public class Junit5Test
{

    @RegisterExtension
    public PolygeneUnitExtension polygene = PolygeneUnitExtension.forModule( module -> {
        module.values(Cat.class );
    } ).build();

    @Structure
    private ValueBuilderFactory vbf;

    @Test
    public void givenPolygeneWhenInstantiatingCatExpectCatInstantiation()
    {
        ValueBuilder<Cat> builder = vbf.newValueBuilder( Cat.class );
        builder.prototype().identity().set( StringIdentity.identityOf( "123" ) );
        builder.prototype().name().set( "Kim" );
        Cat cat = builder.newInstance();

        assertThat( cat, notNullValue() );
        assertThat( cat.name().get(), equalTo("Kim") );
    }
}
