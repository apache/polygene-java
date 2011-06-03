package org.qi4j.api.event;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public class ActivationEvent<T>
{
    public enum EventType
    {
        ACTIVATING,ACTIVATED,PASSIVATING,PASSIVATED
    }

    private long timestamp;
    private T source;
    private EventType type;

    public ActivationEvent( T source, EventType type )
    {
        this.timestamp = System.currentTimeMillis();
        this.source = source;
        this.type = type;
    }

    public T source()
    {
        return source;
    }

    public EventType type()
    {
        return type;
    }

    public String message()
    {
        switch(type)
        {
            case ACTIVATING:
                return "Activating "+source;
            case ACTIVATED:
                return "Activated "+source;
            case PASSIVATING:
                return "Passivating "+source;
            case PASSIVATED:
                return "Passivated "+source;
        }
        return "";
    }


    @Override
    public String toString()
    {
        return message();
    }
}
