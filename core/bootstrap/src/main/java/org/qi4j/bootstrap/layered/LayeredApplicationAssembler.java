/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.bootstrap.layered;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

public abstract class LayeredApplicationAssembler
    implements ApplicationAssembler
{
    protected Application application;
    protected String name;
    protected String version;
    private final Application.Mode mode;
    private ApplicationAssembly assembly;

    private HashMap<Class<? extends LayerAssembler>, LayerAssembler> assemblers = new HashMap<>();

    public LayeredApplicationAssembler( String name, String version, Application.Mode mode )
        throws AssemblyException
    {
        this.name = name;
        this.version = version;
        this.mode = mode;
        Energy4Java qi4j = new Energy4Java();
        ApplicationDescriptor model = qi4j.newApplicationModel( this );
        onModelCreated( model );
        instantiateApplication( qi4j, model );
    }

    /**
     * This method is called from the constructor to instantiate the Qi4j application from the application model.
     *
     * <p>
     * The default implementation simply calls;
     * <pre><code>
     *   application = model.newInstance( qi4j.spi() );
     * </code></pre>
     * </p>
     *
     * @param qi4j  The Qi4j runtime engine.
     * @param model The application model descriptor.
     */
    protected void instantiateApplication( Energy4Java qi4j, ApplicationDescriptor model )
    {
        application = model.newInstance( qi4j.spi() );
    }

    /**
     * This method is called after the Application Model has been created, before the instantiation of the Qi4j
     * application.
     *
     * <p>
     * The default implementation does nothing. Applications may have advanced features to inspect or
     * modify the model prior to instantiation, and this is the place where such advanced manipulation is
     * expected to take place.
     * </p>
     *
     * @param model
     */
    protected void onModelCreated( ApplicationDescriptor model )
    {
    }

    public Application application()
    {
        return application;
    }

    public void start()
        throws ActivationException
    {
        application.activate();
    }

    public void stop()
        throws PassivationException
    {
        application.passivate();
    }

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        assembly = applicationFactory.newApplicationAssembly();
        assembly.setName( name );
        assembly.setVersion( version );
        assembly.setMode( mode );
        assembleLayers( assembly );
        return assembly;
    }

    protected LayerAssembly createLayer( Class<? extends LayerAssembler> layerAssemblerClass )
        throws IllegalArgumentException
    {
        try
        {
            String classname = layerAssemblerClass.getSimpleName();
            if( classname.endsWith( "Layer" ) )
            {
                classname = classname.substring( 0, classname.length() - 5 ) + " Layer";
            }
            setNameIfPresent( layerAssemblerClass, classname );
            LayerAssembly layer = assembly.layer( classname );

            LayerAssembler layerAssembler = instantiateAssembler( layerAssemblerClass, layer );
            assemblers.put( layerAssemblerClass, layerAssembler );
            LayerAssembly assembly = layerAssembler.assemble( layer );
            if( assembly == null )
            {
                // Assume that people forgot, and let's not require a "return layer", since we can do that ourselves.
                return layer;
            }
            return assembly;
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Unable to instantiate layer with " + layerAssemblerClass.getSimpleName(), e );
        }
    }

    private LayerAssembler instantiateAssembler( Class<? extends LayerAssembler> layerAssemblerClass,
                                                 LayerAssembly layer
    )
        throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException
    {
        LayerAssembler layerAssembler;
        try
        {
            Constructor<? extends LayerAssembler> assemblyConstructor = layerAssemblerClass.getConstructor( LayerAssembly.class );
            layerAssembler = assemblyConstructor.newInstance( layer );
        }
        catch( NoSuchMethodException e )
        {
            // Use default constructor then.
            layerAssembler = layerAssemblerClass.newInstance();
        }
        return layerAssembler;
    }

    static void setNameIfPresent( Class<?> clazz, String classname )
        throws IllegalAccessException
    {
        try
        {
            Field field = clazz.getDeclaredField( "NAME" );
            if( Modifier.isStatic( field.getModifiers() ) )
            {
                field.setAccessible( true );
                field.set( null, classname );
            }
        }
        catch( Exception e )
        {
            // Ignore and consider normal.
        }
    }

    @SuppressWarnings( "unchecked" )
    protected <T extends LayerAssembler> T assemblerOf( Class<T> layerAssemblerClass )
    {
        return (T) assemblers.get( layerAssemblerClass );
    }

    /**
     * Called from the constructor to assemble the layers in the applcation.
     *
     * <p>
     * This method must be implemented, and is typically a list of LayerAssmebler instantitations, followed
     * by {@link LayerAssembly#uses(LayerAssembly...)} declarations.
     * <pre><code>
     *
     * </code></pre>
     * </p>
     */
    protected abstract void assembleLayers( ApplicationAssembly assembly )
        throws AssemblyException;
}
