package org.qi4j.test.mock;

import java.util.ArrayList;
import java.util.Collection;

public class MockRecorderMixin
    implements MockRecorder, MockRepository
{

    private final Collection<Mock> mocks;

    public MockRecorderMixin()
    {
        this.mocks = new ArrayList<Mock>();
    }


    public MockRecorderType useMock( Object mock )
    {
        System.out.println( "Recorded " + mock );
        MockProxy proxy = new MockProxy( mock, new UnplayableMock() );
        add( proxy );
        return new MockRecorderTypeImpl( proxy );
    }

    public void add( Mock mock )
    {
        mocks.add( mock );
    }

    public Iterable<Mock> getAll()
    {
        return mocks;
    }
}