/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.api.mixin;

/**
 * This Exception is thrown when no visible Composite implements the MixinType that is
 * requested to be built in the builder.
 */
public class MixinTypeNotAvailableException
    extends MixinMappingException
{
    private static final long serialVersionUID = 6664141678759594339L;

    private final Class<?> mixinType;
    private final String moduleModelName;

    public MixinTypeNotAvailableException( Class<?> mixinType )
    {
        this( mixinType, null );
    }

    public MixinTypeNotAvailableException( Class<?> mixinType, String moduleModelName )
    {
        super( "No visible CompositeType implements MixinType:" + mixinType.getName() );
        this.mixinType = mixinType;
        this.moduleModelName = moduleModelName;
    }

    public Class<?> mixinType()
    {
        return mixinType;
    }

    public String moduleName()
    {
        return moduleModelName;
    }
}
