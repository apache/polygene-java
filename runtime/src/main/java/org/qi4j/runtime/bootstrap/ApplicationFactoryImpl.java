/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.bootstrap;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.HibernatingApplicationInvalidException;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;
import org.qi4j.spi.Qi4jSPI;

/**
 * Factory for ApplicationContext.
 */
public final class ApplicationFactoryImpl
    implements ApplicationFactory, Serializable
{
    private static final String BOOT_FILENAME = "application-model.qi4j";

    private Qi4jSPI runtime;

    public ApplicationFactoryImpl()
    {
        this.runtime = new Qi4jRuntime();
    }

    public Application loadApplication()
        throws HibernatingApplicationInvalidException, AssemblyException
    {
        try
        {
            ApplicationModel model = loadApplicationModelFromFile();
            return createInstance( model );
        }
        catch( IOException e )
        {
            throw new HibernatingApplicationInvalidException( hibernateFile(), e );
        }
        catch( ClassNotFoundException e )
        {
            throw new HibernatingApplicationInvalidException( hibernateFile(), e );
        }
    }

    public Application newApplication( Assembler assembler )
        throws AssemblyException
    {
        return newApplication( new Assembler[][][]{ { { assembler } } } );
    }

    public Application newApplication( Assembler[][][] assemblers )
        throws AssemblyException
    {
        ApplicationAssembly applicationAssembly = newApplicationAssembly();
        applicationAssembly.setName( "Application" );

        // Build all layers bottom-up
        LayerAssembly below = null;
        for( int layer = assemblers.length - 1; layer >= 0; layer-- )
        {
            // Create Layer
            LayerAssembly layerAssembly = applicationAssembly.newLayerAssembly( "Layer " + ( layer + 1 ) );
            for( int module = 0; module < assemblers[ layer ].length; module++ )
            {
                // Create Module
                ModuleAssembly moduleAssembly = layerAssembly.newModuleAssembly( "Module " + ( module + 1 ) );
                for( int assembly = 0; assembly < assemblers[ layer ][ module ].length; assembly++ )
                {
                    // Register Assembler
                    moduleAssembly.addAssembler( assemblers[ layer ][ module ][ assembly ] );
                }
            }
            if( below != null )
            {
                layerAssembly.uses( below ); // Link layers
            }
            below = layerAssembly;
        }
        return newApplication( applicationAssembly );
    }

    public Application newApplication( ApplicationAssembly assembly )
        throws AssemblyException
    {
        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        List<LayerModel> layerModels = new ArrayList<LayerModel>();
        ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.name(), layerModels );
        List<LayerAssemblyImpl> layerAssemblies = new ArrayList<LayerAssemblyImpl>( applicationAssembly.getLayerAssemblies() );
        Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<LayerAssembly, LayerModel>();
        nextLayer:
        while( layerAssemblies.size() > 0 )
        {
            LayerAssemblyImpl layerAssembly = layerAssemblies.remove( 0 );
            Set<LayerAssembly> usesLayers = layerAssembly.getUses();
            List<LayerModel> usedLayers = new ArrayList<LayerModel>();
            for( LayerAssembly usesLayer : usesLayers )
            {
                LayerModel layerModel = mapAssemblyModel.get( usesLayer );
                if( layerModel == null )
                {
                    // Used layer not done yet - reevaluate this layer later
                    layerAssemblies.add( layerAssembly );
                    continue nextLayer;
                }
                usedLayers.add( layerModel );
            }
            UsedLayersModel usedLayersModel = new UsedLayersModel( usedLayers );
            List<ModuleModel> moduleModels = new ArrayList<ModuleModel>();
            String name = layerAssembly.name();
            if( name == null )
            {
                throw new AssemblyException( "Layer must have name set" );
            }
            LayerModel layerModel = new LayerModel( name, usedLayersModel, moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.getModuleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule() );
            }
            mapAssemblyModel.put( layerAssembly, layerModel );
            layerModels.add( layerModel );
        }
// TODO: TypeVariableImpl and ParameterizedTypeImpl are not serializable and causes problems.
//       Removing this section to avoid the vivid Exceptions that otherwise shows up.
//        try
//        {
//            storeApplicationModel( applicationModel );
//        }
//        catch( IOException e )
//        {
//            System.err.println( "Unable to store the application to " + hibernateFile() );
//            e.printStackTrace();
//            hibernateFile().delete();
//        }
        return createInstance( applicationModel );
    }

    public ApplicationAssembly newApplicationAssembly()
    {
        return new ApplicationAssemblyImpl();
    }

    public Qi4jSPI runtime()
    {
        return runtime;
    }

    private Application createInstance( ApplicationModel applicationModel )
        throws AssemblyException
    {
        try
        {
            applicationModel.bind();
        }
        catch( BindingException e )
        {
            throw new AssemblyException( e );
        }

        return applicationModel.newInstance( runtime );
    }


    private ApplicationModel loadApplicationModelFromFile()
        throws IOException, ClassNotFoundException
    {
        File appBootFile = hibernateFile();

            InputStream in = null;
            BufferedInputStream stream = null;
            ObjectInputStream ois = null;
            try
            {
                in = new FileInputStream( appBootFile );
                if( in == null )
                {
                    throw new IllegalArgumentException( appBootFile.getAbsolutePath() + " does not exist, or is not readable." );
                }
                stream = new BufferedInputStream( in );
                ois = new ObjectInputStream( stream );
                return (ApplicationModel) ois.readObject();
            }
            finally
            {
                close( ois );
                close( stream );
                close( in );
            }
    }

    private void storeApplicationModel( ApplicationModel model )
        throws IOException
    {
        File file = hibernateFile();
        FileOutputStream stream = null;
        BufferedOutputStream out = null;
        ObjectOutputStream oos = null;
        try
        {
            stream = new FileOutputStream( file );
            out = new BufferedOutputStream( stream );
            oos = new ObjectOutputStream( out );
            oos.writeObject( model );
            oos.flush();
        }
        finally
        {
            close( oos );
            close( out );
            close( stream );
        }
    }

    private File hibernateFile()
    {
        File dir = new File( "qi4j" ).getAbsoluteFile();
        dir.mkdirs();
        return new File( dir, BOOT_FILENAME );
    }

    private void close( InputStream stream )
        throws IOException
    {
        if( stream != null )
        {
            stream.close();
        }
    }

    private void close( OutputStream stream )
        throws IOException
    {
        if( stream != null )
        {
            stream.close();
        }
    }


}
