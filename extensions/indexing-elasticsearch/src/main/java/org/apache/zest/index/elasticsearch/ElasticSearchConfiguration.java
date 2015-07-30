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
package org.apache.zest.index.elasticsearch;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;

// START SNIPPET: config
public interface ElasticSearchConfiguration
        extends ConfigurationComposite
{

    /**
     * Cluster name.
     * Defaults to 'zest_cluster'.
     */
    @Optional Property<String> clusterName();

    /**
     * Index name.
     * Defaults to 'zest_index'.
     */
    @Optional Property<String> index();

    /**
     * Set to true to index non aggregated associations as if they were aggregated.
     * WARN: Don't use this if your domain model contains circular dependencies.
     * Defaults to 'FALSE'.
     */
    @UseDefaults Property<Boolean> indexNonAggregatedAssociations();

}
// END SNIPPET: config
