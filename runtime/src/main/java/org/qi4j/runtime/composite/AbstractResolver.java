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

package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.spi.composite.ConstraintResolution;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.FieldResolution;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.MethodResolution;
import org.qi4j.spi.composite.ParameterConstraintsModel;
import org.qi4j.spi.composite.ParameterConstraintsResolution;
import org.qi4j.spi.composite.ParameterModel;
import org.qi4j.spi.composite.ParameterResolution;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.ResolutionContext;

/**
 * TODO
 */
public abstract class AbstractResolver
{
    protected void resolveConstructorModel( Iterable<ConstructorModel> constructorModels, List<ConstructorResolution> constructorResolutions, ResolutionContext resolutionContext )
    {
        for( ConstructorModel constructorModel : constructorModels )
        {
            List<ParameterResolution> parameterResolutions = new ArrayList<ParameterResolution>();
            Iterable<ParameterModel> parameterModels = constructorModel.getParameters();
            resolveParameterModels( parameterModels, parameterResolutions, resolutionContext );
            ConstructorResolution constructorResolution = new ConstructorResolution( constructorModel, parameterResolutions );

            constructorResolutions.add( constructorResolution );
        }
    }

    protected void resolveMethodModels( Iterable<MethodModel> methodModels, List<MethodResolution> methodResolutions, ResolutionContext resolutionContext )
    {
        for( MethodModel methodModel : methodModels )
        {
            List<ParameterResolution> parameterResolutions = new ArrayList<ParameterResolution>();
            Iterable<ParameterModel> parameterModels = methodModel.getParameterModels();
            resolveParameterModels( parameterModels, parameterResolutions, resolutionContext );
            MethodResolution methodResolution = new MethodResolution( methodModel, parameterResolutions );

            methodResolutions.add( methodResolution );
        }
    }

    protected void resolveFieldModels( Iterable<FieldModel> fieldModels, List<FieldResolution> fieldResolutions, ResolutionContext resolutionContext )
    {
        for( FieldModel fieldModel : fieldModels )
        {
            InjectionResolution injectionResolution = null;

            InjectionModel injectionModel = fieldModel.getInjectionModel();
            if( injectionModel != null )
            {
                injectionResolution = new InjectionResolution( injectionModel, resolutionContext.getModule(), resolutionContext.getLayer(), resolutionContext.getApplication() );
            }
            FieldResolution fieldResolution = new FieldResolution( fieldModel, injectionResolution );
            fieldResolutions.add( fieldResolution );
        }
    }

    private void resolveParameterModels( Iterable<ParameterModel> parameterModels, List<ParameterResolution> parameterResolutions, ResolutionContext resolutionContext )
    {
        for( ParameterModel parameterModel : parameterModels )
        {
            ParameterConstraintsModel parameterConstraintsModel = parameterModel.getParameterConstraintModel();
            ParameterConstraintsResolution parameterConstraintsResolution = null;
            if( parameterConstraintsModel != null )
            {
                List<ConstraintResolution> constraintResolutions = new ArrayList<ConstraintResolution>();
                // TODO Fill in the list
                parameterConstraintsResolution = new ParameterConstraintsResolution( parameterConstraintsModel, constraintResolutions );
            }

            InjectionResolution injectionResolution = null;
            if( parameterModel.getInjectionModel() != null )
            {
                injectionResolution = new InjectionResolution( parameterModel.getInjectionModel(), resolutionContext.getModule(), resolutionContext.getLayer(), resolutionContext.getApplication() );
            }
            ParameterResolution parameterResolution = new ParameterResolution( parameterModel, parameterConstraintsResolution, injectionResolution );
            parameterResolutions.add( parameterResolution );
        }
    }

}
