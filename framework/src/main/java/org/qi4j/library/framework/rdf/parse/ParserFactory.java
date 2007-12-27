/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.framework.rdf.parse;

import org.openrdf.model.URI;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

public interface ParserFactory
{
    ApplicationParser newApplicationParser();

    LayerParser newLayerParser();

    ModuleParser newModuleParser();

    CompositeParser newCompositeParser();

    ConstraintParser newConstraintParser();

    ConcernParser newConcernParser();

    SideEffectParser newSideEffectParser();

    MixinParser newMixinParser();

    MethodParser newMethodParser();

    ObjectParser newObjectParser();

    CompositeMethodParser newCompositeMethodParser();

    FieldParser newFieldParser();

    ConstructorParser newConstructorParser();

    ParameterParser newParameterParser();

    InjectionParser newInjectionParser();
}
