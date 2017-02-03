package org.apache.polygene.api.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HasTypesCollectorsTest
{
    @Test
    public void selectMatchingTypes()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        List<ValueType> number = valueTypes.stream().collect( HasTypesCollectors.matchingTypes( Number.class ) );
        assertThat( number.size(), is( 2 ) );
        assertThat( number.get( 0 ), equalTo( ValueType.of( Number.class ) ) );
        assertThat( number.get( 1 ), equalTo( ValueType.of( Integer.class ) ) );

        List<ValueType> integer = valueTypes.stream().collect( HasTypesCollectors.matchingTypes( Integer.class ) );
        assertThat( integer.size(), is( 1 ) );
        assertThat( integer.get( 0 ), equalTo( ValueType.of( Integer.class ) ) );
    }

    @Test
    public void selectMatchingType()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Double.class ),
            ValueType.of( Integer.class )
        );

        Optional<ValueType> number = valueTypes.stream()
                                               .collect( HasTypesCollectors.matchingType( Number.class ) );
        assertTrue( number.isPresent() );
        assertThat( number.get(), equalTo( ValueType.of( Double.class ) ) );

        Optional<ValueType> integer = valueTypes.stream()
                                                .collect( HasTypesCollectors.matchingType( Integer.class ) );
        assertTrue( integer.isPresent() );
        assertThat( integer.get(), equalTo( ValueType.of( Integer.class ) ) );
    }

    @Test
    public void selectMatchingValueTypes()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Number.class, Integer.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        List<ValueType> number = valueTypes.stream()
                                           .collect( HasTypesCollectors.matchingTypes( ValueType.of( Number.class ) ) );
        System.out.println( number );
        assertThat( number.size(), is( 2 ) );
        assertThat( number.get( 0 ), equalTo( ValueType.of( Number.class ) ) );
        assertThat( number.get( 1 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );

        List<ValueType> integer = valueTypes.stream()
                                            .collect(
                                                HasTypesCollectors.matchingTypes( ValueType.of( Integer.class ) ) );
        assertThat( integer.size(), is( 2 ) );
        assertThat( integer.get( 0 ), equalTo( ValueType.of( Integer.class ) ) );
        assertThat( integer.get( 1 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );

        List<ValueType> both = valueTypes.stream()
                                         .collect( HasTypesCollectors.matchingTypes( ValueType.of( Number.class,
                                                                                                   Integer.class ) ) );
        assertThat( both.size(), is( 1 ) );
        assertThat( both.get( 0 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );
    }

    @Test
    public void selectMatchingValueType()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Number.class, Integer.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        Optional<ValueType> number = valueTypes.stream()
                                               .collect(
                                                   HasTypesCollectors.matchingType( ValueType.of( Number.class ) ) );
        assertTrue( number.isPresent() );
        assertThat( number.get(), equalTo( ValueType.of( Number.class ) ) );

        Optional<ValueType> integer = valueTypes.stream()
                                                .collect(
                                                    HasTypesCollectors.matchingType( ValueType.of( Integer.class ) ) );
        assertTrue( integer.isPresent() );
        assertThat( integer.get(), equalTo( ValueType.of( Integer.class ) ) );

        Optional<ValueType> both = valueTypes.stream()
                                             .collect( HasTypesCollectors.matchingType( ValueType.of( Number.class,
                                                                                                      Integer.class ) ) );
        assertTrue( both.isPresent() );
        assertThat( both.get(), equalTo( ValueType.of( Number.class, Integer.class ) ) );
    }

    @Test
    public void selectClosestValueTypes()
    {
        List<ValueType> list = new ArrayList<ValueType>()
        {{
            add( ValueType.of( String.class ) );
            add( ValueType.of( Identity.class ) );
        }};

        List<ValueType> result = list.stream()
                                     .collect( HasTypesCollectors.closestTypes( StringIdentity.class ) );
        assertThat( result.size(), is( 1 ) );
        assertThat( result.get( 0 ), equalTo( ValueType.of( Identity.class ) ) );
    }
}
