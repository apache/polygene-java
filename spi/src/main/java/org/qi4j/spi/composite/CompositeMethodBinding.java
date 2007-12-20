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

package org.qi4j.spi.composite;

import java.util.List;
import org.qi4j.spi.entity.property.PropertyBinding;

/**
 * TODO
 */
public class CompositeMethodBinding
{
    private CompositeMethodResolution compositeMethodResolution;
    private Iterable<ParameterBinding> parameterBindings;
    private List<ConcernBinding> concernBindings;
    private List<SideEffectBinding> sideEffectBindings;
    private MixinBinding mixinBinding;
    private PropertyBinding propertyBinding;

    public CompositeMethodBinding( CompositeMethodResolution method, Iterable<ParameterBinding> parameterBindings, List<ConcernBinding> concerns, List<SideEffectBinding> sideEffects, MixinBinding mixin, PropertyBinding propertyBinding )
    {
        this.propertyBinding = propertyBinding;
        this.parameterBindings = parameterBindings;
        this.compositeMethodResolution = method;
        this.concernBindings = concerns;
        this.sideEffectBindings = sideEffects;
        this.mixinBinding = mixin;
    }

    public CompositeMethodResolution getCompositeMethodResolution()
    {
        return compositeMethodResolution;
    }

    public Iterable<ParameterBinding> getParameterBindings()
    {
        return parameterBindings;
    }

    public List<ConcernBinding> getConcernBindings()
    {
        return concernBindings;
    }

    public List<SideEffectBinding> getSideEffectBindings()
    {
        return sideEffectBindings;
    }

    public MixinBinding getMixinBinding()
    {
        return mixinBinding;
    }

    public PropertyBinding getPropertyBinding()
    {
        return propertyBinding;
    }

    @Override public String toString()
    {
        return compositeMethodResolution.getCompositeMethodModel().getMethod().toGenericString();
    }
}
