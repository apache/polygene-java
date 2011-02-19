package org.qi4j.samples.cargo.app1.model.handling;

import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;

/**
 * Thrown when trying to register an event with an unknown tracking id.
 */
public final class UnknownCargoException extends CannotCreateHandlingEventException {

  private final TrackingId trackingId;

  /**
   * @param trackingId cargo tracking id
   */
  public UnknownCargoException(final TrackingId trackingId) {
    this.trackingId = trackingId;
  }

  /**
   * {@inheritDoc}
   */            
  @Override
  public String getMessage() {
    return "No cargo with tracking id " + trackingId + " exists in the system";
  }
}
