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
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.HibernatingApplicationInvalidException;
import org.qi4j.bootstrap.spi.ApplicationFactory;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * Factory for Applications.
 */
public final class ApplicationFactoryImpl
    implements ApplicationFactory
{
    private static final String BOOT_FILENAME = "application-model.qi4j";
    private Qi4jSPI spi;

    public ApplicationFactoryImpl(Qi4jSPI spi)
    {
        this.spi = spi;
    }

    public ApplicationSPI loadApplication()
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

    public ApplicationSPI newApplication( ApplicationAssembly assembly )
        throws AssemblyException
    {
        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        List<LayerModel> layerModels = new ArrayList<LayerModel>();
        ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.name(), applicationAssembly.metaInfo(), layerModels );
        Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<LayerAssembly, LayerModel>();
        Map<LayerAssembly, List<LayerModel>> mapUsedLayers = new HashMap<LayerAssembly, List<LayerModel>>( );

        // Build all layers
        List<LayerAssemblyImpl> layerAssemblies = new ArrayList<LayerAssemblyImpl>( applicationAssembly.getLayerAssemblies() );
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            List<LayerModel> usedLayers = new ArrayList<LayerModel>();
            mapUsedLayers.put( layerAssembly, usedLayers );

            UsedLayersModel usedLayersModel = new UsedLayersModel( usedLayers );
            List<ModuleModel> moduleModels = new ArrayList<ModuleModel>();
            String name = layerAssembly.name();
            if( name == null )
            {
                throw new AssemblyException( "Layer must have name set" );
            }
            LayerModel layerModel = new LayerModel( name, layerAssembly.metaInfo(), usedLayersModel, moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.moduleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule() );
            }
            mapAssemblyModel.put( layerAssembly, layerModel );
            layerModels.add( layerModel );
        }

        // Populate used layer lists
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            Set<LayerAssembly> usesLayers = layerAssembly.uses();
            List<LayerModel> usedLayers = mapUsedLayers.get( layerAssembly );
            for( LayerAssembly usesLayer : usesLayers )
            {
                LayerModel layerModel = mapAssemblyModel.get( usesLayer );
                usedLayers.add( layerModel );
            }
        }

        return createInstance( applicationModel );
    }

    private ApplicationSPI createInstance( ApplicationModel applicationModel )
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

        return applicationModel.newInstance( spi );
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