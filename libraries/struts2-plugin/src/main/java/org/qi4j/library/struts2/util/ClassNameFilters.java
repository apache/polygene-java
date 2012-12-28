package org.qi4j.library.struts2.util;

public final class ClassNameFilters
{
    private ClassNameFilters()
    {
    }

    public static ClassNameMapper passThruMapper = new ClassNameMapper()
    {
        @Override
        public String map( Class<?> type )
        {
            return type.getName();
        }
    };

    public static ClassNameMapper removeSuffixes( final String... suffixes )
    {
        return new ClassNameMapper()
        {
            @Override
            public String map( Class<?> type )
            {
                String className = type.getName();
                for( String suffix : suffixes )
                {
                    if( className.endsWith( suffix ) )
                    {
                        return className.substring( 0, className.length() - suffix.length() );
                    }
                }
                return className;
            }
        };
    }
}
