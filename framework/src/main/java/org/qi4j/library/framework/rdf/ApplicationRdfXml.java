/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.library.framework.rdf;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static org.qi4j.library.framework.rdf.RDFVocabulary.APPLICATION;
import static org.qi4j.library.framework.rdf.RDFVocabulary.CLASS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.COMPOSITE;
import static org.qi4j.library.framework.rdf.RDFVocabulary.CONCERN;
import static org.qi4j.library.framework.rdf.RDFVocabulary.CONCERNS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.FIELD;
import static org.qi4j.library.framework.rdf.RDFVocabulary.FIELDS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.IMPLEMENTED_BY;
import static org.qi4j.library.framework.rdf.RDFVocabulary.INJECTION_SCOPE;
import static org.qi4j.library.framework.rdf.RDFVocabulary.INJECTION_TYPE;
import static org.qi4j.library.framework.rdf.RDFVocabulary.LAYER;
import static org.qi4j.library.framework.rdf.RDFVocabulary.LAYERS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.METHOD;
import static org.qi4j.library.framework.rdf.RDFVocabulary.METHODS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.MIXIN;
import static org.qi4j.library.framework.rdf.RDFVocabulary.MIXINS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.MODULE;
import static org.qi4j.library.framework.rdf.RDFVocabulary.MODULES;
import static org.qi4j.library.framework.rdf.RDFVocabulary.PRIVATE_COMPOSITES;
import static org.qi4j.library.framework.rdf.RDFVocabulary.PUBLIC_COMPOSITES;
import static org.qi4j.library.framework.rdf.RDFVocabulary.QI4JNS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.SIDE_EFFECT;
import static org.qi4j.library.framework.rdf.RDFVocabulary.SIDE_EFFECTS;
import static org.qi4j.library.framework.rdf.RDFVocabulary.USES;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ConcernBinding;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.composite.SideEffectBinding;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.structure.LayerBinding;
import org.qi4j.spi.structure.LayerResolution;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.structure.ModuleResolution;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
<?xml version="1.0"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dc="http://purl.org/dc/elements/1.1/"
         xmlns:ex="http://example.org/stuff/1.0/">
  <rdf:Description rdf:about="http://www.w3.org/TR/rdf-syntax-grammar"
		   dc:title="RDF/XML Syntax Specification (Revised)">
    <ex:editor>
      <rdf:Description ex:fullName="Dave Beckett">
	<ex:homePage rdf:resource="http://purl.org/net/dajobe/" />
      </rdf:Description>
    </ex:editor>
  </rdf:Description>
</rdf:RDF>
*/

/**
 * Generate RDF/XML data given an ApplicationContext.
 */
public class ApplicationRdfXml
{
    // Namespaces
    public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFSNS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String DCNS = "http://purl.org/dc/elements/1.1/";

    // RDF attributes
    public static final String ABOUT = "about";
    public static final String ID = "ID";
    public static final String RESOURCE = "resource";
    public static final String PARSE_TYPE = "parseType";

    private Document m_doc;


    public ApplicationRdfXml( ApplicationContext context )
    {
        m_doc = toXml( context );
    }

    /**
     * @param stream  the OutputStream to write to.
     * @param charset The character set to use, or null if UTF-8.
     */
    public void print( OutputStream stream, Charset charset )
    {
        if( charset == null )
        {
            charset = Charset.forName( "UTF-8" );
        }
        OutputStreamWriter osw = new OutputStreamWriter( stream, charset );
        BufferedWriter bw = new BufferedWriter( osw );
        PrintWriter writer = new PrintWriter( bw );
        print( writer );
    }

    /**
     * @param writer The PrintWriter to write the result to.
     */
    public void print( PrintWriter writer )
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        Source source = new DOMSource( m_doc );
        Result result = new StreamResult( writer );
        try
        {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF" );
            transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.transform( source, result );
        }
        catch( TransformerConfigurationException e )
        {
            e.printStackTrace( writer );
        }
        catch( TransformerException e )
        {
            e.printStackTrace( writer );
        }
    }

    Document toXml( ApplicationContext context )
    {
        try
        {
            String xsltUrl = "application.xsl";
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware( true );
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            doc.appendChild( doc.createProcessingInstruction( "xml-stylesheet", "type=\"text/xsl\" href=\"" + xsltUrl + "\"" ) );

            Element rdf = (Element) doc.appendChild( doc.createElementNS( RDFNS, "RDF" ) );

            addApplication( context, doc, rdf );

            return doc;
        }
        catch( ParserConfigurationException e )
        {
            throw new IllegalStateException( e );
        }
    }

    private void addApplication( ApplicationContext context, Document doc, Element rdf )
    {
        String appId = getApplicationId( context );
        Element application = addResource( doc, rdf, APPLICATION, appId );
        addTitle( doc, application, appId );

        Element layers = addCollection( doc, application, LAYERS );
        Iterable<LayerBinding> layerBindings = context.getApplicationBinding().getLayerBindings();
        for( LayerBinding layerBinding : layerBindings )
        {
            addLayer( layerBinding, doc, layers, appId );
        }
    }

    private void addLayer( LayerBinding layerBinding, Document doc, Element layers, String appId )
    {
        String layerId = getLayerId( layerBinding.getLayerResolution() );
        Element layer = addResource( doc, layers, LAYER, layerId );
        addTitle( doc, layer, layerBinding.getLayerResolution().getLayerModel().getName() );

        Iterable<LayerResolution> uses = layerBinding.getLayerResolution().getUses();
        for( LayerResolution usesLayer : uses )
        {
            addResourceProperty( doc, layer, USES, appId + "/" + getLayerId( usesLayer ) );
        }

        Element modules = addCollection( doc, layer, MODULES );
        Iterable<ModuleBinding> moduleBindings = layerBinding.getModuleBindings();
        for( ModuleBinding moduleBinding : moduleBindings )
        {
            addModule( moduleBinding, doc, modules );
        }
    }

    private void addModule( ModuleBinding moduleBinding, Document doc, Element modules )
    {
        String moduleId = getModuleId( moduleBinding.getModuleResolution() );
        Element module = addResource( doc, modules, MODULE, moduleId );
        addTitle( doc, module, moduleBinding.getModuleResolution().getModuleModel().getName() );

//                    for( Map.Entry<Class<? extends Composite>, ModuleModel> entry : moduleBinding.getModuleResolution().getInstantiableComposites().entrySet() )
//                    {
//                        String instantiableComposite = moduleBinding.getModuleResolution().
//                        addResourceProperty(doc, module, INSTANTIABLE_COMPOSITES, entry.getKey().getName());
//                    }

        Element publicComposites = addCollection( doc, module, PUBLIC_COMPOSITES );
        Iterable<CompositeModel> composites = moduleBinding.getModuleResolution().getModuleModel().getPublicComposites();
        for( CompositeModel composite : composites )
        {
            addComposite( composite, moduleBinding, publicComposites, doc );
        }

        Element privateComposites = addCollection( doc, module, PRIVATE_COMPOSITES );
        Iterable<CompositeModel> privateCompositeModels = moduleBinding.getModuleResolution().getModuleModel().getPrivateComposites();
        for( CompositeModel privateCompositeModel : privateCompositeModels )
        {
            addComposite( privateCompositeModel, moduleBinding, privateComposites, doc );
        }
    }

    private void addComposite( CompositeModel compositeModel, ModuleBinding moduleBinding, Element compositeCollection, Document doc )
    {
        CompositeBinding compositeBinding = moduleBinding.getCompositeBindings().get( compositeModel.getCompositeClass() );
        Element composite = addResource( doc, compositeCollection, COMPOSITE, getCompositeId( compositeBinding ) );
        addTitle( doc, composite, compositeModel.getCompositeClass().getSimpleName() );

        Element compositeMethods = addCollection( doc, composite, METHODS );
        for( CompositeMethodBinding compositeMethodBinding : compositeBinding.getCompositeMethodBindings() )
        {
            Element compositeMethod = addResource( doc, compositeMethods, METHOD, getMethodId( compositeMethodBinding ) );
            String title = compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod().toGenericString();
            title = title.split( " ", 3 )[ 2 ];
            addTitle( doc, compositeMethod, title );
            Element concerns = addCollection( doc, compositeMethod, CONCERNS );
            for( ConcernBinding concernBinding : compositeMethodBinding.getConcernBindings() )
            {
                Element concern = addResource( doc, concerns, CONCERN, getCompositeId( compositeBinding ) + "/" + getConcernId( concernBinding ) );
                addTitle( doc, concern, concernBinding.getConcernResolution().getConcernModel().getModelClass().getSimpleName() );
                addProperty( doc, concern, QI4JNS, CLASS, concernBinding.getConcernResolution().getConcernModel().getModelClass().getName() );
            }
            Element sideEffects = addCollection( doc, compositeMethod, SIDE_EFFECTS );
            for( SideEffectBinding sideEffectBinding : compositeMethodBinding.getSideEffectBindings() )
            {
                Element sideEffect = addResource( doc, sideEffects, SIDE_EFFECT, getCompositeId( compositeBinding ) + "/" + getSideEffectId( sideEffectBinding ) );
                addTitle( doc, sideEffect, sideEffectBinding.getSideEffectResolution().getSideEffectModel().getModelClass().getName() );
                addProperty( doc, sideEffect, QI4JNS, CLASS, sideEffectBinding.getSideEffectResolution().getSideEffectModel().getModelClass().getName() );
            }
            addResourceProperty( doc, compositeMethod, IMPLEMENTED_BY, getCompositeId( compositeBinding ) + "/" + getMixinId( compositeMethodBinding.getMixinBinding() ) );
        }

        Element mixins = addCollection( doc, composite, MIXINS );
        for( MixinBinding mixinBinding : compositeBinding.getMixinBindings() )
        {
            Element mixin = addResource( doc, mixins, MIXIN, getMixinId( mixinBinding ) );
            addTitle( doc, mixin, mixinBinding.getMixinResolution().getMixinModel().getModelClass().getSimpleName() );
            addProperty( doc, mixin, QI4JNS, CLASS, mixinBinding.getMixinResolution().getMixinModel().getModelClass().getName() );

            Iterable<FieldBinding> fieldBindings = mixinBinding.getFieldBindings();
            Element fields = addCollection( doc, mixin, FIELDS );
            for( FieldBinding fieldBinding : fieldBindings )
            {
                Element field = addResource( doc, fields, FIELD, getFieldId( fieldBinding ) );
                addTitle( doc, field, fieldBinding.getFieldResolution().getFieldModel().getField().getName() );
                addProperty( doc, field, QI4JNS, INJECTION_SCOPE, fieldBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() );
                addProperty( doc, field, QI4JNS, INJECTION_TYPE, fieldBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionType().toString() );
            }
        }
    }

    private void addTitle( Document doc, Element resource, String title )
    {
//        addProperty(doc, resource, DCNS, "title", title);
        addProperty( doc, resource, RDFSNS, "label", title );
    }

    private void addProperty( Document doc, Element resource, String namespace, String name, String value )
    {
        Element type = (Element) resource.appendChild( doc.createElementNS( namespace, name ) );
        type.appendChild( doc.createTextNode( value ) );
    }

    private Element addResource( Document doc, Element owner, String type, String resourceId )
    {
        Element resource = (Element) owner.appendChild( doc.createElementNS( RDFVocabulary.QI4JNS, type ) );
        addAttribute( doc, resource, ID, resourceId );
        return resource;
    }

    private Element addCollection( Document doc, Element resource, String collectionName )
    {
        Element collection = (Element) resource.appendChild( doc.createElementNS( RDFVocabulary.QI4JNS, collectionName ) );
        addAttribute( doc, collection, PARSE_TYPE, "Collection" );
        return collection;
    }

    private void addResourceProperty( Document doc, Element subject, String predicate, String resource )
    {
        Element use = (Element) subject.appendChild( doc.createElementNS( RDFVocabulary.QI4JNS, predicate ) );
        addAttribute( doc, use, RESOURCE, resource );
    }

    private String getApplicationId( ApplicationContext context )
    {
        return context.getApplicationBinding().getApplicationResolution().getApplicationModel().getName();
    }

    private String getLayerId( LayerResolution layerResolution )
    {
        return layerResolution.getApplicationModel().getName() + "/" + layerResolution.getLayerModel().getName();
    }

    private String getModuleId( ModuleResolution moduleResolution )
    {
        return moduleResolution.getApplicationModel().getName() + "/" + moduleResolution.getLayerModel().getName() + "/" + moduleResolution.getModuleModel().getName();
    }

    private String getCompositeId( CompositeBinding compositeBinding )
    {
        return escape( compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass().getName() );
    }

    private String getMethodId( CompositeMethodBinding compositeMethodBinding )
    {
        String name = compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod().toString().split( " " )[ 3 ];
        name = escape( name );
        return name;
    }

    private String getMixinId( MixinBinding mixinBinding )
    {
        return escape( mixinBinding.getMixinResolution().getMixinModel().getModelClass().getName() );
    }

    private String getConcernId( ConcernBinding concernBinding )
    {
        return escape( concernBinding.getConcernResolution().getConcernModel().getModelClass().getName() );
    }

    private String getSideEffectId( SideEffectBinding sideEffectBinding )
    {
        return escape( sideEffectBinding.getSideEffectResolution().getSideEffectModel().getModelClass().getName() );
    }

    private String getFieldId( FieldBinding fieldBinding )
    {
        InjectionResolution injectionResolution = fieldBinding.getInjectionBinding().getInjectionResolution();
        return injectionResolution.getApplication().getName() + "/" +
               injectionResolution.getLayer().getName() + "/" +
               injectionResolution.getModule().getName() + "/" +
               escape( injectionResolution.getCompositeModel().getCompositeClass().getName() ) + "/" +
               escape( injectionResolution.getAbstractModel().getModelClass().getName() ) + "/" +
               fieldBinding.getFieldResolution().getFieldModel().getField().getName();
    }

    private String escape( String name )
    {
        name = name.replaceAll( "[^A-Za-z]", "_" );
        return name;
    }

    private void addAttribute( Document doc, Element element, String name, String value )
    {
        Attr attr = doc.createAttributeNS( RDFNS, name );
        attr.setValue( value );
        element.getAttributes().setNamedItem( attr );
    }
}
