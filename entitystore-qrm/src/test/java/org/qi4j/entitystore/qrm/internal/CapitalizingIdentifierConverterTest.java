package org.qi4j.entitystore.qrm.internal;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.qi4j.api.common.QualifiedName;

import static org.junit.Assert.*;

public class CapitalizingIdentifierConverterTest
{
    private final CapitalizingIdentifierConverter converter = new CapitalizingIdentifierConverter();

    @Test
    public void convertToUpperCase()
    {
        assertEquals( "identity -> ID", "ID", converter.convertIdentifier( QualifiedName.fromQN( "abc:identity" ) ) );
        assertEquals( "uppercase ABC", "ABC", converter.convertIdentifier( QualifiedName.fromQN( "abc:abc" ) ) );
        assertEquals( "removed qualified prefix", "ABC", converter.convertIdentifier( QualifiedName.fromQN( "aaa:abc" ) ) );
        assertEquals( "removed qualified prefixes", "ABC", converter.convertIdentifier( QualifiedName.fromQN( "aaa:bbb:abc" ) ) );
    }

    @Test
    public void removeFromMap()
    {
        Map<String, Object> rawData = new HashMap<String, Object>();
        rawData.put( "ABC", "test" );
        assertEquals( "converted key and found value", "test", converter.getValueFromData( rawData, QualifiedName.fromQN( "aaa:abc" ) ) );
        assertEquals( "entry removed", 0, rawData.size() );
    }

    @Test
    public void nullIfNotFound()
    {
        Map<String, Object> rawData = new HashMap<String, Object>();
        assertEquals( "converted key and found value", null, converter.getValueFromData( rawData, QualifiedName.fromQN( "aaa:abc" ) ) );
    }

    @Test
    public void convertMapKeys()
    {
        Map<QualifiedName, Object> rawData = new HashMap<QualifiedName, Object>();
        rawData.put( QualifiedName.fromQN( "abc:abc" ), "test1" );
        rawData.put( QualifiedName.fromQN( "abc:DEF" ), "test2" );
        rawData.put( QualifiedName.fromQN( "aaa:GHI" ), "test3" );
        rawData.put( QualifiedName.fromQN( "aaa:bbb:JKL" ), "test4" );
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
        rawData.put( QualifiedName.fromQN( "abc" ), "test1" );
        rawData.put( QualifiedName.fromQN( "ABC" ), "test2" );
        converter.convertKeys( rawData );
    }

    @Test( expected = IllegalArgumentException.class )
    public void failDuplicateQualfiedKeys()
    {
        Map<QualifiedName, Object> rawData = new HashMap<QualifiedName, Object>();
        rawData.put( QualifiedName.fromQN( "abc" ), "test1" );
        rawData.put( QualifiedName.fromQN( "aaa:abc" ), "test2" );
        converter.convertKeys( rawData );
    }
}
