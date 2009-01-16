/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.unitofwork;

/**
 * Filter change events based on the qualified name of the change
 */
public class StateChangeQualifiedNameFilter
    implements StateChangeListener, StateChangeVoter
{
    private String qualifiedName;
    private StateChangeVoter voterDelegate;
    private StateChangeListener listenerDelegate;

    public StateChangeQualifiedNameFilter( String qualifiedName, StateChangeVoter delegate )
    {
        this.qualifiedName = qualifiedName;
        this.voterDelegate = delegate;
    }

    public StateChangeQualifiedNameFilter( String qualifiedName, StateChangeListener delegate )
    {
        this.qualifiedName = qualifiedName;
        this.listenerDelegate = delegate;
    }

    public void acceptChange( StateChange change ) throws ChangeVetoException
    {
        if (change.qualifiedName().equals(qualifiedName))
            voterDelegate.acceptChange( change );
    }

    public void notify( StateChange change )
    {
        if (change.qualifiedName().equals(qualifiedName))
            listenerDelegate.notify( change );
    }
}