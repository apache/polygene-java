package se.citerus.dddsample.application.remoting.dto.assembler;

import java.util.ArrayList;
import java.util.List;
import se.citerus.dddsample.application.remoting.dto.LocationDTO;
import se.citerus.dddsample.domain.model.location.Location;

public class LocationDTOAssembler {

  public LocationDTO toDTO(Location location) {
    return new LocationDTO(location.unLocode().idString(), location.name());
  }

  public List<LocationDTO> toDTOList(List<Location> allLocations) {
    final List<LocationDTO> dtoList = new ArrayList<LocationDTO>(allLocations.size());
    for (Location location : allLocations) {
      dtoList.add(toDTO(location));
    }
    return dtoList;
  }
}
