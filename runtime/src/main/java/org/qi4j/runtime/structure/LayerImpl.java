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

package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.structure.Application;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public class LayerImpl
    implements Layer
{
    Application application;
    List<Layer> usages = new ArrayList<Layer>();
    List<Layer> uses = new ArrayList<Layer>();
    Iterable<Module> modules;

    public LayerImpl( Iterable<Module> modules )
    {
        this.modules = modules;
    }

    public Application getApplication()
    {
        return application;
    }

    public Iterable<Layer> getUsages()
    {
        return usages;
    }

    public Iterable<Layer> getUses()
    {
        return uses;
    }

    public Iterable<Module> getModules()
    {
        return modules;
    }

    void setApplication( Application application )
    {
        this.application = application;
    }

    void addUsage( Layer usedBy )
    {
        this.usages.add( usedBy );
    }

    void addUses( Layer use )
    {
        this.uses.add( use );
    }
}
