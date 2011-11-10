package org.qi4j.library.struts2.util;

public final class ClassNameFilters
{
    private ClassNameFilters()
    {
    }

    public static ClassNameFilter passThruFilter = new ClassNameFilter()
    {
        public String filter( String className )
        {
            return className;
        }
    };

    public static ClassNameFilter removeSuffixes( final String... suffixes )
    {
        return new ClassNameFilter()
        {
            public String filter( String className )
            {
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
