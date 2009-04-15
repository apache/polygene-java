/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j.state;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
class ManyAssociationFactory
{
    private static final Map<StateName, ManyAssociationFactory> cache = new HashMap<StateName, ManyAssociationFactory>();


    static ManyAssociationFactory getFactory(ManyAssociationType model)
    {
        StateName qName = model.stateName();
        ManyAssociationFactory value = cache.get(qName);
        if (value == null)
        {
            synchronized (cache)
            {
                value = cache.get(qName);
                if (value == null)
                {
                    cache.put(qName, value = new ManyAssociationFactory(model));
                }
            }
        }
        return value;
    }

    public static ManyAssociationFactory load(StateName qName, String typeString)
    {
        ManyAssociationFactory value = cache.get(qName);
        if (value == null)
        {
            synchronized (cache)
            {
                value = cache.get(qName);
                if (value == null)
                {
                    cache.put(qName, value = new ManyAssociationFactory(qName, typeString));
                }
            }
        }
        return value;
    }

    private final StateName stateName;
    private final CollectionFactory factory;

    private ManyAssociationFactory(ManyAssociationType model)
    {
        this.stateName = model.stateName();
        this.factory = CollectionFactory.getFactoryFor();
    }

    public ManyAssociationFactory(StateName stateName, String typeString)
    {
        this.stateName = stateName;
        this.factory = CollectionFactory.getFactoryFor();
    }

    StateName getStateName()
    {
        return stateName;
    }

    ManyAssociationState createNodeCollection(DirectEntityState state, NeoService neo, NeoIdentityIndex idIndex)
    {
        return factory.createNodeCollection(this, state, neo, idIndex);
    }

    static boolean isManyAssociation(AssociationDescriptor model)
    {
        return ManyAssociation.class.isAssignableFrom(model.accessor().getReturnType());
    }

    RelationshipType createAssociationType(LinkType type)
    {
        return type.getRelationshipType(stateName.qualifiedName().name());
    }

    public String typeString()
    {
        // TODO
        return "???";
    }
}
