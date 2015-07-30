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
package org.apache.zest.index.elasticsearch.filesystem;

import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.index.elasticsearch.ElasticSearchFinder;
import org.apache.zest.index.elasticsearch.ElasticSearchIndexer;
import org.apache.zest.index.elasticsearch.ElasticSearchSupport;

@Mixins( ESFilesystemSupport.class )
public interface ESFilesystemIndexQueryService
        extends ElasticSearchIndexer, ElasticSearchFinder, ElasticSearchSupport, ServiceComposite
{
}
