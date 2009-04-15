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

import org.qi4j.api.entity.EntityReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectIdentityList implements IndirectCollection
{
    private final List<ListElement> backend;
    private List<EntityReference> underlyingList;

    public IndirectIdentityList(List<EntityReference> underlyingList)
    {
        this.underlyingList = underlyingList;
        backend = new ArrayList<ListElement>(underlyingList.size());
        Iterator<EntityReference> iter = underlyingList.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            backend.add(new ListElement(iter.next(), i));
        }
    }

    public int count()
    {
        return backend.size();
    }

    public boolean contains(EntityReference entityReference)
    {
        return backend.contains(entityReference);
    }

    public boolean remove(EntityReference entityReference)
    {
        return backend.remove(entityReference);
    }

    public Iterator<EntityReference> iterator()
    {
        return underlyingList.iterator();
    }

    public EntityReference get(int i)
    {
        return backend.get(i).value;
    }


    public EntityReference set(int i, EntityReference entityReference)
    {
        ListElement old = backend.get(i);
        if (old.value.equals(entityReference))
        {
            return entityReference;
        }
        return backend.set(i, new ListElement(entityReference)).value;
    }

    public boolean add(int i, EntityReference entityReference)
    {
        backend.add(i, new ListElement(entityReference));
        return true;
    }

    public EntityReference remove(int i)
    {
        return backend.remove(i).value;
    }

    public int size()
    {
        return backend.size();
    }

    public void prepareCommit()
    {
        int next = 0;
        ListIterator<EntityReference> iter = underlyingList.listIterator();
        for (ListElement element : backend)
        {
            if (element.index < 0)
            {
                iter.add(element.value);
            } else
            {
                iter.next();
                while (element.index > next)
                {
                    iter.remove();
                    iter.next();
                    next++;
                }
                next++;
            }
        }
        while (iter.hasNext())
        {
            iter.next();
            iter.remove();
        }
    }

    private static class ListElement
    {
        private final EntityReference value;
        private final int index;

        ListElement(EntityReference id, int index)
        {
            this.value = id;
            this.index = index;
        }

        ListElement(EntityReference id)
        {
            this(id, -1);
        }
    }

}
