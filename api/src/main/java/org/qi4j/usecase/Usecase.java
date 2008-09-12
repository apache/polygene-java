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

import java.io.Serializable;

/**
 * A Usecase. A Usecase is used as a model for UnitOfWork, and helps
 * implementations decide what to do in certain circumstances.
 */
public class Usecase
    implements Serializable
{
    public static final Usecase DEFAULT = new Usecase("Default", CAP.CA, new StateUsage(false));

    private String name;
    private CAP guarantees;
    private StateUsage stateUsage;

    Usecase( String name, CAP guarantees, StateUsage stateUsage )
    {
        this.name = name;
        this.guarantees = guarantees;
        this.stateUsage = stateUsage;
    }

    public String name()
    {
        return name;
    }

    public CAP guarantees()
    {
        return guarantees;
    }

    public StateUsage stateUsage()
    {
        return stateUsage;
    }

    @Override public String toString()
    {
        return name + ", guarantees "+guarantees+", uses "+stateUsage;
    }
}
