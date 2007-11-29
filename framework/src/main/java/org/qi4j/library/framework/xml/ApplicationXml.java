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

import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.qi4j.Composite;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ConcernBinding;
import org.qi4j.spi.structure.LayerBinding;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.structure.ModuleModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO
 */
public class ApplicationXml
{
    Document toXml( ApplicationContext context )
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element app = (Element) doc.appendChild( doc.createElement( "application" ) );
            addAttribute( doc, app, "id", context.getApplicationBinding().getApplicationResolution().getApplicationModel().getName() );

            Iterable<LayerBinding> layers = context.getApplicationBinding().getLayerBindings();
            for( LayerBinding layerBinding : layers )
            {
                Element layer = (Element) app.appendChild( doc.createElement( "layer" ) );
                addAttribute( doc, layer, "id", layerBinding.getLayerResolution().getLayerModel().getName() );

                Iterable<LayerModel> uses = layerBinding.getLayerResolution().getUses();
                for( LayerModel useModel : uses )
                {
                    Element use = (Element) layer.appendChild( doc.createElement( "uses" ) );
                    addAttribute( doc, use, "idref", useModel.getName() );
                }

                Iterable<ModuleBinding> moduleBindings = layerBinding.getModuleBindings();
                for( ModuleBinding moduleBinding : moduleBindings )
                {
                    Element module = (Element) layer.appendChild( doc.createElement( "module" ) );
                    addAttribute( doc, module, "idref", moduleBinding.getModuleResolution().getModuleModel().getName() );

                    Element instantiableComposites = (Element) module.appendChild( doc.createElement( "instantiablecomposites" ) );
                    for( Map.Entry<Class<? extends Composite>, ModuleModel> entry : moduleBinding.getModuleResolution().getInstantiableComposites().entrySet() )
                    {
                        Element instantiableComposite = (Element) instantiableComposites.appendChild( doc.createElement( "instantiablecomposite" ) );
                        addAttribute( doc, instantiableComposite, "type", entry.getKey().getName() );
                        addAttribute( doc, instantiableComposite, "idref", entry.getValue().getName() );
                    }

                    Element publicComposites = (Element) module.appendChild( doc.createElement( "publiccomposites" ) );
                    Iterable<CompositeModel> composites = moduleBinding.getModuleResolution().getModuleModel().getPublicComposites();
                    addComposites( composites, moduleBinding, publicComposites, doc );

                    Element privateComposites = (Element) module.appendChild( doc.createElement( "privatecomposites" ) );
                    Iterable<CompositeModel> privateCompositeModels = moduleBinding.getModuleResolution().getModuleModel().getPrivateComposites();
                    addComposites( privateCompositeModels, moduleBinding, privateComposites, doc );
                }
            }

            return doc;
        }
        catch( ParserConfigurationException e )
        {
            throw new IllegalStateException( e );
        }
    }

    private void addComposites( Iterable<CompositeModel> composites, ModuleBinding moduleBinding, Element compositesElement, Document doc )
    {
        for( CompositeModel compositeModel : composites )
        {
            CompositeBinding compositeBinding = moduleBinding.getCompositeBindings().get( compositeModel.getCompositeClass() );
            Element composite = (Element) compositesElement.appendChild( doc.createElement( "composite" ) );
            addTextElement( doc, composite, "type", compositeModel.getCompositeClass().getName() );

            for( CompositeMethodBinding compositeMethodBinding : compositeBinding.getCompositeMethodBindings() )
            {
                Element compositeMethod = (Element) composite.appendChild( doc.createElement( "method" ) );
                addAttribute( doc, compositeMethod, "signature", compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod().toGenericString() );
                Element concerns = (Element) compositeMethod.appendChild( doc.createElement( "concerns" ) );
                for( ConcernBinding concernBinding : compositeMethodBinding.getConcernBindings() )
                {
                    Element concern = (Element) concerns.appendChild( doc.createElement( "method" ) );
                    addAttribute( doc, concern, "type", concernBinding.getConcernResolution().getConcernModel().getModelClass().getName() );
                }

                Element methodMixin = (Element) compositeMethod.appendChild( doc.createElement( "mixin" ) );
                addAttribute( doc, methodMixin, "type", compositeMethodBinding.getMixinBinding().getMixinResolution().getMixinModel().getModelClass().getName() );
            }

        }
    }

    private void addTextElement( Document doc, Element composite, String name, String value )
    {
        Element type = (Element) composite.appendChild( doc.createElement( name ) );
        type.appendChild( doc.createTextNode( value ) );
    }

    private void addAttribute( Document doc, Element element, String name, String value )
    {
        Attr attr = doc.createAttribute( name );
        attr.setValue( value );
        element.getAttributes().setNamedItem( attr );
    }
}
