/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import org.elasticsearch.client.Client;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;

public interface ElasticSearchSupport
        extends ServiceActivation
{

    Client client();

    String index();

    String entitiesType();

    boolean indexNonAggregatedAssociations();

    SpatialConfiguration.Configuration spatialConfiguration();

    Module getModule();

}
