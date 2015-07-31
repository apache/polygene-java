/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.communication.query.dto;

import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.conversion.DTO;

/**
 * Voyage DTO
 *
 * Since all properties of Voyage are immutable, we can simply re-use the same interface.
 * We need the Voyage as a DTO when we do entity to value conversions.
 */
public interface VoyageDTO extends Voyage, DTO
{
}
