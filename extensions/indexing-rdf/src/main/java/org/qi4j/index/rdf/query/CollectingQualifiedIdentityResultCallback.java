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

import java.util.ArrayList;
import java.util.Collection;
import org.qi4j.api.entity.EntityReference;

public class CollectingQualifiedIdentityResultCallback
    implements QualifiedIdentityResultCallback
{
    private final Collection<EntityReference> entities = new ArrayList<EntityReference>();

    @Override
    public boolean processRow( long row, EntityReference entityReference )
    {
        entities.add( entityReference );
        return true;
    }

    public Collection<EntityReference> entities()
    {
        return entities;
    }
}
