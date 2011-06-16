package org.qi4j.api.event;

/**
 * ActivationEvents are fired during activation and passivation of instances in Qi4j.
 */
public final class ActivationEvent
{
    public enum EventType
    {
        ACTIVATING,ACTIVATED,PASSIVATING,PASSIVATED
    }

    private long timestamp;
    private Object source;
    private EventType type;

    public ActivationEvent( Object source, EventType type )
    {
        this.timestamp = System.currentTimeMillis();
        this.source = source;
        this.type = type;
    }

    public Object source()
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
