package org.qi4j.manual.recipes.properties.pojo;


// START SNIPPET: mutableBook
public interface MutableBook extends Book
{
    void setTitle( String title );
    void setAuthor( String author );
}
// END SNIPPET: mutableBook