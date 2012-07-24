package org.qi4j.tutorials.services.step3;

public interface Library
{
    void createInitialData();
    
    Book borrowBook( String author, String title );

    void returnBook( Book book );
}