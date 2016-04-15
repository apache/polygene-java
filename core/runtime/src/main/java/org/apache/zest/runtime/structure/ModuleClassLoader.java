/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.runtime.structure;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.ModelDescriptor;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.module;
import static org.apache.zest.api.util.Classes.modelTypeSpecification;

// Module ClassLoader
class ModuleClassLoader
    extends ClassLoader
{

    private final ModuleModel moduleModel;
    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>();

    ModuleClassLoader( ModuleModel moduleModel, ClassLoader classLoader )
    {
        super( classLoader );
        this.moduleModel = moduleModel;
    }

    @Override
    protected Class<?> findClass( String className )
        throws ClassNotFoundException
    {
        try
        {
            Class<?> resultingClass = classes.computeIfAbsent( className, name ->
            {
                Predicate<ModelDescriptor> modelTypeSpecification = modelTypeSpecification( name );
                Stream<? extends ModelDescriptor> moduleModels = concat(
                    moduleModel.visibleObjects( Visibility.module ),
                    concat(
                        moduleModel.visibleEntities( Visibility.module ),
                        concat(
                            moduleModel.visibleTransients( Visibility.module ),
                            moduleModel.visibleValues( Visibility.module )
                        )
                    )
                ).filter( modelTypeSpecification );

                Class<?> clazz = null;
                Iterator<? extends ModelDescriptor> iterator = moduleModels.iterator();
                if( iterator.hasNext() )
                {
                    clazz = iterator.next().types().findFirst().orElse( null );

                    if( iterator.hasNext() )
                    {
                        // Ambiguous exception
                        new AmbiguousTypeException(
                            "More than one model matches the classname " + name + ":" + iterator.next()
                        );
                    }
                }

                // Check layer
                if( clazz == null )
                {
                    Stream<? extends ModelDescriptor> modelsInLayer1 = concat(
                        moduleModel.layer().visibleObjects( Visibility.layer ),
                        concat(
                            moduleModel.layer().visibleEntities( Visibility.layer ),
                            concat(
                                moduleModel.layer().visibleTransients( Visibility.layer ),
                                moduleModel.layer().visibleValues( Visibility.layer )
                            )
                        )
                    );
                    // TODO: What does this actually represents?? Shouldn't 'application' visible models already be handed back from lasyerInstance().visibleXyz() ??
                    Stream<? extends ModelDescriptor> modelsInLayer2 = concat(
                        moduleModel.layer().visibleObjects( Visibility.application ),
                        concat(
                            moduleModel.layer().visibleEntities( Visibility.application ),
                            concat(
                                moduleModel.layer().visibleTransients( Visibility.application ),
                                moduleModel.layer().visibleValues( Visibility.application )
                            )
                        )
                    );
                    Stream<? extends ModelDescriptor> layerModels = concat(
                        modelsInLayer1,
                        modelsInLayer2
                    ).filter( modelTypeSpecification );

                    Iterator<? extends ModelDescriptor> layerModelsIter = layerModels.iterator();
                    if( layerModelsIter.hasNext() )
                    {
                        clazz = layerModelsIter.next().types().findFirst().orElse( null );

                        if( layerModelsIter.hasNext() )
                        {
                            // Ambiguous exception
                            new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + layerModelsIter.next()
                            );
                        }
                    }
                }

                // Check used layers
                if( clazz == null )
                {
                    Stream<? extends ModelDescriptor> usedLayersModels = concat(
                        moduleModel.layer()
                            .usedLayers()
                            .layers()
                            .flatMap( layer -> layer.visibleObjects( module ) ),
                        concat(
                            moduleModel.layer()
                                .usedLayers()
                                .layers()
                                .flatMap( layer -> layer.visibleEntities( Visibility.layer ) ),
                            concat(
                                moduleModel.layer()
                                    .usedLayers()
                                    .layers()
                                    .flatMap( layer -> layer.visibleTransients( application ) ),
                                moduleModel.layer()
                                    .usedLayers()
                                    .layers()
                                    .flatMap( layer -> layer.visibleValues( application ) )
                            )
                        )
                    ).filter( modelTypeSpecification );

                    Iterator<? extends ModelDescriptor> usedLayersModelsIter = usedLayersModels.iterator();
                    if( usedLayersModelsIter.hasNext() )
                    {
                        clazz = usedLayersModelsIter.next().types().findFirst().orElse( null );

                        if( usedLayersModelsIter.hasNext() )
                        {
                            // Ambiguous exception
                            new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + usedLayersModelsIter.next()
                            );
                        }
                    }
                }
                return clazz;
            } );
            if( resultingClass == null )
            {
                throw new ClassNotFoundException();
            }
            return resultingClass;
        }
        catch( AmbiguousTypeException e )
        {
            throw new ClassNotFoundException( className, e );
        }
    }
}
