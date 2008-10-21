/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.usecase;

/**
 * Builder for Usecases.
 */
public class UsecaseBuilder
{
    public static UsecaseBuilder buildUsecase()
    {
        return new UsecaseBuilder();
    }

    private CAP guarantees = CAP.CP;
    private String name;
    private StateUsage stateUsage;

    private UsecaseBuilder()
    {
    }

    public UsecaseBuilder named( String aName )
    {
        name = aName;
        return this;
    }

    public UsecaseBuilder withGuarantees( CAP cap )
    {
        guarantees = cap;
        return this;
    }

    public UsecaseBuilder uses( StateUsage stateUsage )
    {
        this.stateUsage = stateUsage;
        return this;
    }

    public Usecase newUsecase()
    {
        StateUsage usecaseStateUsage = stateUsage;
        if( usecaseStateUsage == null )
        {
            usecaseStateUsage = new StateUsage( true );
        }

        return new Usecase( name, guarantees, usecaseStateUsage );
    }
}
