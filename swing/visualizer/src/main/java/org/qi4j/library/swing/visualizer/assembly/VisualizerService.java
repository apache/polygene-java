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
package org.qi4j.library.swing.visualizer.assembly;

import org.qi4j.composite.Mixins;
import org.qi4j.injection.scope.Structure;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceComposite;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationVisualization;
import org.qi4j.library.swing.visualizer.Qi4jApplicationVisualizer;
import org.qi4j.structure.Application;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( VisualizerService.VisualizerServiceMixin.class )
public interface VisualizerService extends Activatable, ServiceComposite
{
    class VisualizerServiceMixin
        implements Activatable
    {
        @Structure Application application;
        @Structure private ObjectBuilderFactory obf;
        private Qi4jApplicationVisualizer visualizer;

        public void activate()
            throws Exception
        {
            visualizer = obf.newObject( Qi4jApplicationVisualizer.class );
            visualizer.show( application );
        }

        public void passivate()
            throws Exception
        {
            visualizer.hide();
            visualizer = null;
        }
    }
}
