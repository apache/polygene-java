package org.qi4j.library.spring.bootstrap;

public class ToUppercaseService
        implements TextProcessingService
{

    public String process( final String text )
    {
        return text.toUpperCase();
    }

}
