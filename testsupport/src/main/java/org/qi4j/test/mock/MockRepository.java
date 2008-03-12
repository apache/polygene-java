package org.qi4j.test.mock;

public interface MockRepository
{
    void add( Mock mock );

    Iterable<Mock> getAll();
}