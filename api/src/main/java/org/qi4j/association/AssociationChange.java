/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.association;

/**
 * TODO
 */
public final class AssociationChange<R, T>
{
    ChangeType changeType;
    R referer;
    T reference;

    public AssociationChange( ChangeType changeType, R referer, T reference )
    {
        this.referer = referer;
        this.changeType = changeType;
        this.reference = reference;
    }

    public ChangeType getChangeType()
    {
        return changeType;
    }

    public R getReferer()
    {
        return referer;
    }

    public T getReference()
    {
        return reference;
    }
}
