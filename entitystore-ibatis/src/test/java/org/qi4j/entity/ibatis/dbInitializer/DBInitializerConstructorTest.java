package org.qi4j.entity.ibatis.dbInitializer;

import java.util.Properties;
import org.junit.Test;
import org.jmock.Mockery;

/**
 * @autor Michael Hunger
 * @since 18.05.2008
 */
public class DBInitializerConstructorTest
{

    @Test public void testValidConstructor()
    {
        final DBInitializerConfiguration config = new Mockery().mock( DBInitializerConfiguration.class );
        new DBInitializer( config );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorNullArgument()
    {
        new DBInitializer( null );
    }
}
