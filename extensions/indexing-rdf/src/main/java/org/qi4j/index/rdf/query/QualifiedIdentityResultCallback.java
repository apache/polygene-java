/*
 * Copyright 2008 Michael Hunger.
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
package org.qi4j.index.rdf.query;

import org.qi4j.api.entity.EntityReference;

public interface QualifiedIdentityResultCallback
{
    /**
     * @param row             the current row of the resultset
     * @param entityReference The entity reference found via the query.
     *
     * @return true if resultset processing should stop.
     */
    boolean processRow( long row, EntityReference entityReference );
}
