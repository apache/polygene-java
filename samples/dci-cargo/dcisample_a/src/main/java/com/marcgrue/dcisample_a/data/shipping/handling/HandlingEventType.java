package com.marcgrue.dcisample_a.data.shipping.handling;


/**
 * A handling event type either requires or prohibits a voyage association.
 */
public enum HandlingEventType
{
    RECEIVE( false ),
    LOAD( true ),
    UNLOAD( true ),
    CUSTOMS( false ),
    CLAIM( false );

    private final boolean voyageRequired;

    /**
     * Private enum constructor.
     *
     * @param voyageRequired whether or not a voyage is associated with this event type
     */
    private HandlingEventType( final boolean voyageRequired )
    {
        this.voyageRequired = voyageRequired;
    }

    /**
     * @return True if a voyage association is required for this event type.
     */
    public boolean requiresVoyage()
    {
        return voyageRequired;
    }

    /**
     * @return True if a voyage association is prohibited for this event type.
     */
    public boolean prohibitsVoyage()
    {
        return !requiresVoyage();
    }
}
