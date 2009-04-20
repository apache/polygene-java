/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.value;

import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.io.Serializable;

/**
 * JAVADOC
 */
public interface ValueType
    extends Serializable
{
    public TypeName type();

    void versionize( SchemaVersion schemaVersion );

    public void toJSON( Object value, StringBuilder json, Qi4jSPI spi );

    public Object fromJSON( PeekableStringTokenizer json, Module module );
}
