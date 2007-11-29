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

/**
 * TODO
 */
public class CompositeMethodResolution
{
    private CompositeMethodModel compositeMethodModel;
    private Iterable<ParameterResolution> parameterResolutions;
    private Iterable<ConcernResolution> concernResolutions;
    private Iterable<SideEffectResolution> sideEffectResolutions;
    private MixinResolution mixinResolution;

    public CompositeMethodResolution( CompositeMethodModel methodModel, Iterable<ParameterResolution> parameterResolutions, Iterable<ConcernResolution> concerns, Iterable<SideEffectResolution> sideEffects, MixinResolution mixin )
    {
        this.compositeMethodModel = methodModel;
        this.parameterResolutions = parameterResolutions;
        this.concernResolutions = concerns;
        this.sideEffectResolutions = sideEffects;
        this.mixinResolution = mixin;
    }

    public CompositeMethodModel getCompositeMethodModel()
    {
        return compositeMethodModel;
    }

    public Iterable<ParameterResolution> getParameterResolutions()
    {
        return parameterResolutions;
    }

    public Iterable<ConcernResolution> getConcernResolutions()
    {
        return concernResolutions;
    }

    public Iterable<SideEffectResolution> getSideEffectResolutions()
    {
        return sideEffectResolutions;
    }

    public MixinResolution getMixinResolution()
    {
        return mixinResolution;
    }
}
