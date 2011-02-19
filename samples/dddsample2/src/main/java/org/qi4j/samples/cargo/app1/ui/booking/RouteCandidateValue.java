package org.qi4j.samples.cargo.app1.ui.booking;

import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;

/**
 * DTO for presenting and selecting an itinerary from a collection of candidates.
 */
public interface RouteCandidateValue extends Itinerary.State, ValueComposite
{
}