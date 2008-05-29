/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public final class ParametersModel
{
    private List<ParameterModel> parameters = new ArrayList<ParameterModel>();

    public ParametersModel( List<ParameterModel> parameters )
    {
        this.parameters = parameters;
    }

    public void checkConstraints( Object[] params )
    {
        // Check constraints
        for( int j = 0; j < parameters.size(); j++ )
        {
            ParameterModel parameterModel = parameters.get( j );
            parameterModel.checkConstraints( params[ j ] );
        }
    }
}