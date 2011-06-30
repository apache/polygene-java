package org.qi4j.runtime.types;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.json.JSONObjectSerializer;
import org.qi4j.api.type.ValueType;

import static org.junit.Assert.assertEquals;

public class JodaLocalDateTypeTest
{

    private ValueType underTest;

    @Before
    public void setup()
    {
        underTest = new ValueType( LocalDate.class );
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( new LocalDate( "2020-03-04" ), underTest );
        Object value = serializer.getRoot();
        assertEquals( "2020-03-04", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = new JSONDeserializer( null ).deserialize( "2020-03-04", underTest );
        assertEquals( new LocalDate("2020-03-04"), value);
    }
}
