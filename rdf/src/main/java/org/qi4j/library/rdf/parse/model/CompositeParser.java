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
package org.qi4j.library.rdf.parse.model;

import org.qi4j.library.rdf.parse.ParseContext;

public final class CompositeParser
{
    private final ParseContext context;

    public CompositeParser( ParseContext context )
    {
        this.context = context;
    }

/*    public URI parseModel( LayerModel layerModel, ModuleModel moduleModel, CompositeModel model )
    {
        URI compositeNode = context.createCompositeUri( layerModel, moduleModel, model.getCompositeType() );
        if( Entity.class.isAssignableFrom( model.getCompositeType() ) )
        {
            context.addType( compositeNode, Qi4jRdf.TYPE_ENTITY );
        }
        parseCompositeMethodModels( compositeNode, model );
        parseConstraintModels( compositeNode, model );
        parseConcernModels( compositeNode, model );
        parseSideEffectModels( compositeNode, model );
        parseMixinModels( compositeNode, model );
        parseThisModels( compositeNode, model );
        return compositeNode;
    }

    private void parseCompositeMethodModels( URI compositeNode, CompositeModel model )
    {
        CompositeMethodParser parser = context.getParserFactory().newCompositeMethodParser();
        for( CompositeMethodModel methodModel : model.getCompositeMethodModels() )
        {
            Value method = parser.parseModel( methodModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATION_METHOD, method );
        }
    }

    private void parseConstraintModels( URI compositeNode, CompositeModel model )
    {
        ConstraintParser parser = context.getParserFactory().newConstraintParser();
        for( ConstraintModel constraintModel : model.getConstraintModels() )
        {
            Value constraint = parser.parseModel( constraintModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATIONSHIP_CONSTRAINT, constraint );
        }
    }

    private void parseConcernModels( URI compositeNode, CompositeModel model )
    {
        ConcernParser parser = context.getParserFactory().newConcernParser();
        for( ConcernModel concernModel : model.getConcernModels() )
        {
            Value concern = parser.parseModel( concernModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATIONSHIP_CONCERN, concern );
        }

    }

    private void parseSideEffectModels( URI compositeNode, CompositeModel model )
    {
        SideEffectParser parser = context.getParserFactory().newSideEffectParser();
        for( SideEffectModel sideeffectModel : model.getSideEffectModels() )
        {
            Value sideeffect = parser.parseModel( sideeffectModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATIONSHIP_SIDEEFFECT, sideeffect );
        }

    }

    private void parseMixinModels( URI compositeNode, CompositeModel model )
    {
        MixinParser parser = context.getParserFactory().newMixinParser();
        for( MixinModel mixinModel : model.getMixinModels() )
        {
            Value mixin = parser.parseModel( mixinModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATIONSHIP_MIXIN, mixin );
        }

    }

    private void parseThisModels( URI compositeNode, CompositeModel model )
    {
        CompositeMethodParser parser = context.getParserFactory().newCompositeMethodParser();
        for( CompositeMethodModel methodModel : model.getThisModels() )
        {
            Value method = parser.parseModel( methodModel );
            context.addRelationship( compositeNode, Qi4jRdf.RELATIONSHIP_PRIVATE_METHOD, method );
        }
    }*/
}
