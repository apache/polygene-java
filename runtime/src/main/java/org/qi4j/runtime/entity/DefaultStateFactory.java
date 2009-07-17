/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.runtime.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.StateFactory;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.entity.EntityStoreEvents;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

public class DefaultStateFactory
    implements StateFactory
{
    public EntityState createEntityState( EntityStoreUnitOfWork unitOfWork,
                                          String version,
                                          long lastModified,
                                          EntityReference identity,
                                          EntityStatus status,
                                          Set<EntityTypeReference> entityTypes,
                                          Map<StateName, String> properties,
                                          Map<StateName, EntityReference> associations,
                                          Map<StateName, List<EntityReference>> manyAssociations )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, version, lastModified, identity,
                                       status, entityTypes, properties, associations, manyAssociations
        );
    }

    public EntityState createEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity );
    }

    public EntityStoreUnitOfWork createEntityStoreUnitOfWork( EntityStoreEvents entityStoreEvents,
                                                              String uowesId,
                                                              Usecase usecase,
                                                              MetaInfo metaInfo )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreEvents, uowesId, usecase, metaInfo );
    }
}
