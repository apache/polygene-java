package org.qi4j.samples.cargo.app1.model.handling;

import org.qi4j.samples.cargo.app1.model.voyage.VoyageNumber;

/**
 * Thrown when trying to register an event with an unknown carrier movement id.
 */
public class UnknownVoyageException extends CannotCreateHandlingEventException
{

    private final VoyageNumber voyageNumber;

    public UnknownVoyageException( VoyageNumber voyageNumber )
    {
        this.voyageNumber = voyageNumber;
    }

    @Override
    public String getMessage()
    {
        return "No voyage with number " + voyageNumber + " exists in the system";
    }
}
