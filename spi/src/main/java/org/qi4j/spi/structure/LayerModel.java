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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.composite.Composite;

/**
 *
 */
public final class LayerModel
    implements Serializable
{
    private Iterable<ModuleModel> moduleModels;
    private Map<Class<? extends Composite>, ModuleModel> publicCompositeMap;
    private String name;

    public LayerModel( Iterable<ModuleModel> modules, Map<Class<? extends Composite>, ModuleModel> publicCompositeMap, String name )
    {
        this.publicCompositeMap = publicCompositeMap;
        this.name = name;
        this.moduleModels = modules;
    }

    public Iterable<ModuleModel> getModuleModels()
    {
        return moduleModels;
    }

    public Map<Class<? extends Composite>, ModuleModel> getPublicCompositeMap()
    {
        return publicCompositeMap;
    }

    public String getName()
    {
        return name;
    }
}
