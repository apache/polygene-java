package org.qi4j.samples.dddsample.application.remoting.assembly;

import org.qi4j.samples.dddsample.application.remoting.dto.LocationDTO;

/**
 * Location DTO.
 */
final class LocationDTOImpl
    implements LocationDTO
{
    private final String unLocode;
    private final String name;

    LocationDTOImpl( String unLocode, String name )
    {
        this.unLocode = unLocode;
        this.name = name;
    }

    public String getUnLocode()
    {
        return unLocode;
    }

    public String getName()
    {
        return name;
    }
}