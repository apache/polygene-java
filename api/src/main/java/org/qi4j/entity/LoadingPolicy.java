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

package org.qi4j.entity;

import java.io.Serializable;
import java.util.Set;

/**
 * TODO
 */
public final class LoadingPolicy
    implements Serializable
{
    private static final long serialVersionUID = -4845530533299793091L;

    private Set<Class<? extends EntityComposite>> loadCompositeTypes;
    private Set<Class<?>> loadMixinTypes;
    private Set<String> loadProperties;
    private boolean recording;

    public Set<Class<? extends EntityComposite>> loadCompositeTypes()
    {
        return loadCompositeTypes;
    }

    public Set<Class<?>> loadMixinTypes()
    {
        return loadMixinTypes;
    }

    public Set<String> loadProperties()
    {
        return loadProperties;
    }

    public void setRecording( boolean isRecording )
    {
        recording = isRecording;
    }

    public boolean isRecording()
    {
        return recording;
    }
}
