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

package org.qi4j.library.framework.xml;

/**
 * This is the RDF vocabulary for Qi4j.
 */
public interface RDFVocabulary
{
    // Namespace
    public static final String QI4JNS = "http://www.qi4j.org/rdf/1.0#";

    // Types
    public static final String APPLICATION = "application";
    public static final String LAYER = "layer";
    public static final String MODULE = "module";
    public static final String COMPOSITE = "composite";
    public static final String METHOD = "method";
    public static final String CONCERN = "concern";
    public static final String SIDE_EFFECT = "sideeffect";
    public static final String MIXIN = "mixin";
    public static final String FIELD = "field";

    //Properties
    public static final String LAYERS = "layers";
    public static final String USES = "uses";
    public static final String MODULES = "modules";
    public static final String INSTANTIABLE_COMPOSITES = "instantiablecomposites";
    public static final String PUBLIC_COMPOSITES = "publiccomposites";
    public static final String PRIVATE_COMPOSITES = "privatecomposites";
    public static final String METHODS = "methods";
    public static final String CONCERNS = "concerns";
    public static final String SIDE_EFFECTS = "sideeffects";
    public static final String IMPLEMENTED_BY = "implementedby";
    public static final String MIXINS = "mixins";
    public static final String FIELDS = "fields";
    public static final String CLASS = "class";
    public static final String INJECTION_SCOPE = "injectionscope";
    public static final String INJECTION_TYPE = "injectiontype";

}
