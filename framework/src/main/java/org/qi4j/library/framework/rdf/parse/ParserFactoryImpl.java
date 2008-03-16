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

import org.qi4j.library.framework.rdf.parse.model.ApplicationParser;
import org.qi4j.library.framework.rdf.parse.model.CompositeMethodParser;
import org.qi4j.library.framework.rdf.parse.model.CompositeParser;
import org.qi4j.library.framework.rdf.parse.model.ConcernParser;
import org.qi4j.library.framework.rdf.parse.model.ConstraintParser;
import org.qi4j.library.framework.rdf.parse.model.ConstructorParser;
import org.qi4j.library.framework.rdf.parse.model.FieldParser;
import org.qi4j.library.framework.rdf.parse.model.InjectionParser;
import org.qi4j.library.framework.rdf.parse.model.LayerParser;
import org.qi4j.library.framework.rdf.parse.model.MethodParser;
import org.qi4j.library.framework.rdf.parse.model.MixinParser;
import org.qi4j.library.framework.rdf.parse.model.ModuleParser;
import org.qi4j.library.framework.rdf.parse.model.ObjectParser;
import org.qi4j.library.framework.rdf.parse.model.ParameterParser;
import org.qi4j.library.framework.rdf.parse.model.SideEffectParser;
import org.qi4j.library.framework.rdf.parse.model.ServiceParser;

public final class ParserFactoryImpl
    implements ParserFactory
{
    private ParseContext context;

    public ParserFactoryImpl()
    {
    }

    public ApplicationParser newApplicationParser()
    {
        return new ApplicationParser( context );
    }

    public LayerParser newLayerParser()
    {
        return new LayerParser( context );
    }

    public ModuleParser newModuleParser()
    {
        return new ModuleParser( context );
    }

    public CompositeParser newCompositeParser()
    {
        return new CompositeParser( context );
    }

    public ConstraintParser newConstraintParser()
    {
        return new ConstraintParser( context );
    }

    public ConcernParser newConcernParser()
    {
        return new ConcernParser( context );
    }

    public SideEffectParser newSideEffectParser()
    {
        return new SideEffectParser( context );
    }

    public MixinParser newMixinParser()
    {
        return new MixinParser( context );
    }

    public MethodParser newMethodParser()
    {
        return new MethodParser( context );
    }

    public ObjectParser newObjectParser()
    {
        return new ObjectParser( context );
    }

    public CompositeMethodParser newCompositeMethodParser()
    {
        return new CompositeMethodParser( context );
    }

    public FieldParser newFieldParser()
    {
        return new FieldParser( context );
    }

    public ConstructorParser newConstructorParser()
    {
        return new ConstructorParser( context );
    }

    public ParameterParser newParameterParser()
    {
        return new ParameterParser( context );
    }

    public InjectionParser newInjectionParser()
    {
        return new InjectionParser( context );
    }

    public ServiceParser newServiceParser()
    {
        return new ServiceParser( context );
    }

    public void setParseContext( ParseContext context )
    {
        this.context = context;
    }
}
