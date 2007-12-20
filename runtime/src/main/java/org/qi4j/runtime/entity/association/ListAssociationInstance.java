/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.entity.association;

import java.util.AbstractList;
import java.util.List;
import org.qi4j.entity.association.AssociationAccessObserver;
import org.qi4j.entity.association.AssociationChangeObserver;
import org.qi4j.entity.association.ListAssociation;

/**
 * TODO
 */
public class ListAssociationInstance<T>
    extends AbstractList<T>
    implements ListAssociation<T>
{
    List<T> associated;

    public ListAssociationInstance( List<T> associated )
    {
        this.associated = associated;
    }

    public T get( int i )
    {
        return associated.get( i );
    }

    public int size()
    {
        return associated.size();
    }

    public <T> T getAssociationInfo( Class<T> infoType )
    {
        // TODO
        return null;
    }

    public void addChangeObserver( AssociationChangeObserver<?, T> associationChangeObserver )
    {
        // TODO
    }

    public void addAccessObserver( AssociationAccessObserver<?, T> associationAccessObserver )
    {
        // TODO
    }
}
