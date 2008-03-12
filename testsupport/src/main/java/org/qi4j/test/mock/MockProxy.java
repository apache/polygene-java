package org.qi4j.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MockProxy
    implements Mock
{
    private final Object recordedMock;
    private Mock mock;

    MockProxy( Object recordedMock, Mock mock )
    {
        this.recordedMock = recordedMock;
        this.mock = mock;
    }

    MockProxy setMock( Mock mock )
    {
        this.mock = mock;
        return this;
    }

    public Object getRecordedMock()
    {
        return recordedMock;
    }

    public InvocationHandler getInvocationHandler( Object proxy, Method method, Object[] args )
    {
        return mock.getInvocationHandler( proxy, method, args );
    }
}