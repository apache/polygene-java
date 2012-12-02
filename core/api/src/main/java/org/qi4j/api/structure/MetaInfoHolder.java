/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.api.structure;

/**
 * MetaInfo holder.
 */
public interface MetaInfoHolder
{

    /**
     * Get metadata that implements the given type.
     * The info is registered during assembly of the application.
     *
     * @param infoType the type of metadata to be returned
     *
     * @return the metadata for the given type, or <code>null</code> if
     *         no such metadata has been registered
     */
    <T> T metaInfo( Class<T> infoType );
}
