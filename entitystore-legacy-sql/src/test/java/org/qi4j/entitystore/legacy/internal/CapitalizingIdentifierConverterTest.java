package org.qi4j.entitystore.legacy.internal;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.qi4j.entitystore.legacy.internal.CapitalizingIdentifierConverter;
import org.qi4j.api.common.QualifiedName;
import java.util.Map;
import java.util.HashMap;

/**
 * @author mh14 @ jexp.de
 * @since 11.06.2008 03:18:24 (c) 2008 jexp.de
 */
public class CapitalizingIdentifierConverterTest
{
    private final CapitalizingIdentifierConverter converter = new CapitalizingIdentifierConverter();

    @Test public void convertToUpperCase()
    {
        assertEquals( "identity -> ID", "ID", converter.convertIdentifier( new QualifiedName( "abc:identity" ) ) );
        assertEquals( "uppercase ABC", "ABC", converter.convertIdentifier( new QualifiedName( "abc:abc" ) ) );
        assertEquals( "removed qualified prefix", "ABC", converter.convertIdentifier( new QualifiedName( "aaa:abc" ) ) );
        assertEquals( "removed qualified prefixes", "ABC", converter.convertIdentifier( new QualifiedName( "aaa:bbb:abc" ) ) );
    }

    @Test public void removeFromMap()
    {
        Map<String, Object> rawData = new HashMap<String, Object>();
        rawData.put( "ABC", "test" );
        assertEquals( "converted key and found value", "test", converter.getValueFromData( rawData, new QualifiedName( "aaa:abc" ) ) );
        assertEquals( "entry removed", 0, rawData.size() );
    }

    @Test public void nullIfNotFound()
    {
        Map<String, Object> rawData = new HashMap<String, Object>();
        assertEquals( "converted key and found value", null, converter.getValueFromData( rawData, new QualifiedName( "aaa:abc" ) ) );
    }

    @Test public void convertMapKeys()
    {
        Map<QualifiedName, Object> rawData = new HashMap<QualifiedName, Object>();
        rawData.put( new QualifiedName( "abc:abc" ), "test1" );
        rawData.put( new QualifiedName( "abc:DEF" ), "test2" );
        rawData.put( new QualifiedName( "aaa:GHI" ), "test3" );
        rawData.put( new QualifiedName( "aaa:bbb:JKL" ), "test4" );
        final Map<String, Object> convertedData = converter.convertKeys( rawData );
        assertEquals( "all entries remained", 4, convertedData.size() );
        assertEquals( "converted key and found value ABC", "test1", convertedData.get( "ABC" ) );
        assertEquals( "converted key and found value DEF", "test2", convertedData.get( "DEF" ) );
        assertEquals( "converted key and found value GHI", "test3", convertedData.get( "GHI" ) );
        assertEquals( "converted key and found value JKL", "test4", convertedData.get( "JKL" ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void failDuplicateKeys()
    {
        Map<QualifiedName, Object> rawData = new HashMap<QualifiedName, Object>();
        rawData.put( new QualifiedName( "abc" ), "test1" );
        rawData.put( new QualifiedName( "ABC" ), "test2" );
        converter.convertKeys( rawData );
    }

    @Test( expected = IllegalArgumentException.class )
    public void failDuplicateQualfiedKeys()
    {
        Map<QualifiedName, Object> rawData = new HashMap<QualifiedName, Object>();
        rawData.put( new QualifiedName( "abc" ), "test1" );
        rawData.put( new QualifiedName( "aaa:abc" ), "test2" );
        converter.convertKeys( rawData );
    }
}
