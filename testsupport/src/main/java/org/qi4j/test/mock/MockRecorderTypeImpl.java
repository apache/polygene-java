package org.qi4j.test.mock;

public class MockRecorderTypeImpl
    implements MockRecorderType
{
    private final MockProxy proxy;

    public MockRecorderTypeImpl( MockProxy proxy )
    {
        this.proxy = proxy;
    }

    public void forClass( final Class clazz )
    {
        proxy.setMock( new MethodClassMatcherMock( proxy.getRecordedMock(), clazz ) );
    }

}