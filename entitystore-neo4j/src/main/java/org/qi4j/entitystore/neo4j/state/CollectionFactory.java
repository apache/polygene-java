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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.ManyAssociationState;

import java.util.Collection;
import java.util.List;

enum CollectionFactory implements BackendFactory
{
    LIST(Duplicates.NOT_ALLOWED)
            {
                @Override
                ManyAssociationState createNodeCollection(ManyAssociationFactory factory, DirectEntityState state, NeoService neo, NeoIdentityIndex idIndex)
                {
                    return new DirectIdentityList(neo, idIndex, state, factory.getQualifiedName());
                }

                IndirectCollection createPreloadedCollection(Collection<EntityReference> manyAssociation)
                {
                    return new IndirectIdentityList((List<EntityReference>) manyAssociation);
                }

                public <E> Collection<E> createBackend(Class<E> elementType)
                {
                    return null;
                }
            };

    private static enum Duplicates implements DuplicationChecker
    {
        ALLOWED
                {
                    public boolean goodToAdd(Iterable<EntityReference> iterable, EntityReference entityReference)
                    {
                        return true;
                    }
                },
        NOT_ALLOWED
                {
                    public boolean goodToAdd(Iterable<EntityReference> iterable, EntityReference entityReference)
                    {
                        for (EntityReference reference : iterable)
                        {
                            if (reference.equals(entityReference))
                            {
                                return false;
                            }
                        }
                        return true;
                    }
                }
    }

    private final DuplicationChecker checker;

    CollectionFactory(DuplicationChecker checker)
    {
        this.checker = checker;
    }

    static CollectionFactory getFactoryFor()
    {
        return LIST;
    }

    ManyAssociationState createNodeCollection(ManyAssociationFactory factory, DirectEntityState state, NeoService neo, NeoIdentityIndex idIndex)
    {
        return new DirectUnorderedCollection(idIndex, checker, state, factory.getQualifiedName());
    }

}
