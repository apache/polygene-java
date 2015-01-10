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

import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;

// START SNIPPET: config
public interface ElasticSearchConfiguration
        extends ConfigurationComposite
{

    /**
     * Cluster name.
     * Defaults to 'qi4j_cluster'.
     */
    @Optional Property<String> clusterName();

    /**
     * Index name.
     * Defaults to 'qi4j_index'.
     */
    @Optional Property<String> index();

    /**
     * Set to true to index non aggregated associations as if they were aggregated.
     * WARN: Don't use this if your domain model contains circular dependencies.
     * Defaults to 'FALSE'.
     */
    @UseDefaults Property<Boolean> indexNonAggregatedAssociations();


    @Optional
    Property<SpatialConfiguration.Configuration> spatial();


}
// END SNIPPET: config
