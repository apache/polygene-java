/*
 * Copyright 2014 Niclas Hedhman.
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.bootstrap.builder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.ApplicationPassivationThread;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * Application Builder.
 */
public class ApplicationBuilder
    implements ActivationEventListenerRegistration
{
    private final String applicationName;
    private final Map<String, LayerDeclaration> layers = new HashMap<>();
    private final List<ActivationEventListener> activationListeners = new ArrayList<>();

    public ApplicationBuilder( String applicationName )
    {
        this.applicationName = applicationName;
    }

    /**
     * Create and activate a new Application.
     * @return Activated Application
     * @throws AssemblyException if the assembly failed
     * @throws ActivationException if the activation failed
     */
    public Application newApplication()
        throws AssemblyException, ActivationException
    {
        Energy4Java qi4j = new Energy4Java();
        ApplicationDescriptor model = qi4j.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory factory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = factory.newApplicationAssembly();
                assembly.setName( applicationName );
                HashMap<String, LayerAssembly> createdLayers = new HashMap<>();
                for( Map.Entry<String, LayerDeclaration> entry : layers.entrySet() )
                {
                    LayerAssembly layer = entry.getValue().createLayer( assembly );
                    createdLayers.put( entry.getKey(), layer );
                }
                for( LayerDeclaration layer : layers.values() )
                {
                    layer.initialize( createdLayers );
                }
                return assembly;
            }
        } );
        Application application = model.newInstance( qi4j.api() );
        for( ActivationEventListener activationListener : activationListeners )
        {
            application.registerActivationEventListener( activationListener );
        }
        beforeActivation();
        application.activate();
        afterActivation();
        return application;
    }

    /**
     * Called before application activation.
     */
    protected void beforeActivation()
    {
    }

    /**
     * Called after application activation.
     */
    protected void afterActivation()
    {
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationListeners.add( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationListeners.remove( listener );
    }

    /**
     * Declare Layer.
     * @param layerName Name of the Layer
     * @return Layer declaration for the given name, new if did not already exists
     */
    public LayerDeclaration withLayer( String layerName )
    {
        LayerDeclaration layerDeclaration = layers.get( layerName );
        if( layerDeclaration != null )
        {
            return layerDeclaration;
        }
        layerDeclaration = new LayerDeclaration( layerName );
        layers.put( layerName, layerDeclaration );
        return layerDeclaration;
    }

    /**
     * Load an ApplicationBuilder from a JSON String.
     * @param json JSON String
     * @return Application Builder loaded from JSON
     * @throws JSONException if unable to read JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( String json )
        throws JSONException, AssemblyException
    {
        JSONObject root = new JSONObject( json );
        return fromJson( root );
    }

    /**
     * Load an ApplicationBuilder from a JSON InputStream.
     * @param json JSON input
     * @return Application Builder loaded from JSON
     * @throws JSONException if unable to read JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( InputStream json )
        throws JSONException, AssemblyException
    {
        String jsonString = new Scanner( json, "UTF-8" ).useDelimiter( "\\A" ).next();
        return fromJson( jsonString );
    }

    /**
     * Load an ApplicationBuilder from a JSONObject.
     * @param root JSON object
     * @return Application Builder loaded from JSON
     * @throws JSONException if unable to read JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( JSONObject root )
        throws JSONException, AssemblyException
    {
        String applicationName = root.getString( "name" );
        ApplicationBuilder builder = new ApplicationBuilder( applicationName );
        JSONArray layers = root.optJSONArray( "layers" );
        if( layers != null )
        {
            for( int i = 0; i < layers.length(); i++ )
            {
                JSONObject layerObject = layers.getJSONObject( i );
                String layerName = layerObject.getString( "name" );
                LayerDeclaration layerDeclaration = builder.withLayer( layerName );
                JSONArray using = layerObject.optJSONArray( "uses" );
                if( using != null )
                {
                    for( int j = 0; j < using.length(); j++ )
                    {
                        layerDeclaration.using( using.getString( j ) );
                    }
                }
                JSONArray modules = layerObject.optJSONArray( "modules" );
                if( modules != null )
                {
                    for( int k = 0; k < modules.length(); k++ )
                    {
                        JSONObject moduleObject = modules.getJSONObject( k );
                        String moduleName = moduleObject.getString( "name" );
                        ModuleDeclaration moduleDeclaration = layerDeclaration.withModule( moduleName );
                        JSONArray assemblers = moduleObject.optJSONArray( "assemblers" );
                        if( assemblers != null )
                        {
                            for( int m = 0; m < assemblers.length(); m++ )
                            {
                                moduleDeclaration.withAssembler( assemblers.getString( m ) );
                            }
                        }
                    }
                }
            }
        }
        return builder;
    }

    /**
     * {@literal main} method that read JSON from STDIN.
     * <p>Passivation exceptions are written to STDERR if any.</p>
     * @param args Unused
     * @throws JSONException if unable to read JSON
     * @throws AssemblyException if the assembly failed
     * @throws ActivationException if the activation failed
     */
    public static void main( String[] args )
        throws JSONException, ActivationException, AssemblyException
    {
        ApplicationBuilder builder = fromJson( System.in );
        Application application = builder.newApplication();
        Runtime.getRuntime().addShutdownHook( new ApplicationPassivationThread( application, System.err ) );
    }
}
