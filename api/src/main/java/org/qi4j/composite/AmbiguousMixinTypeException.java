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
package org.qi4j.composite;

/** This Exception is thrown when more than one Composite implements a MixinType that one tries
 * to use to create a Composite instance from.
 * <p>
 * For instance;
 * </p>
 * <code><pre>
 * public interface AbcComposite extends Composite, Abc
 * {}
 *
 * public interface DefComposite extends Composite, Def
 * {}
 *
 * public interface Abc
 * {}
 *
 * public interface Def extends Abc
 * {}
 *
 *
 * CompositeBuilder cb = factory.newCompositeBuilder( Abc.class );
 * </pre></code>
 * <p>
 * In the code above, both the AbcComposite and DefComposite implements Abc, and therefor
 * the <code>newCompositeBuilder</code> method can not unambiguously figure out which one is intended.
 * </p>
 */
public class AmbiguousMixinTypeException extends MixinMappingException
{
    private final Class mixinType;

    public AmbiguousMixinTypeException( Class mixinType )
    {
        super( "More than one visible CompositeType implements MixinType: " + mixinType.getName() );
        this.mixinType = mixinType;
    }

    public Class getMixinType()
    {
        return mixinType;
    }
}
