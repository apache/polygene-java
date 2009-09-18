/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.spring.bootstrap;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public final class Qi4jTestBootstrap
        extends Qi4jApplicationBootstrap
        implements ApplicationContextAware
{
    private static final String LAYER = "layer";

    private static final String MODULE = "module";

    static final String COMMENT_SERVICE_ID = "commentService";

    private static final String TO_UPPERCASE_SERVICE_ID = "toUppercaseService";

    private ApplicationContext applicationContext;

    public final void assemble( ApplicationAssembly applicationAssembly ) throws AssemblyException
    {
        LayerAssembly layerAssembly = applicationAssembly.layerAssembly( LAYER );
        ModuleAssembly moduleAssembly = layerAssembly.moduleAssembly( MODULE );
        moduleAssembly.addServices( CommentServiceComposite.class ).identifiedBy( COMMENT_SERVICE_ID );
        // inject Spring bean as a service
        moduleAssembly.importServices( TextProcessingService.class ).setMetaInfo(
                this.applicationContext.getBean( TO_UPPERCASE_SERVICE_ID ) );
    }

    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}
