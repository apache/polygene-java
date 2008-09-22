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
package org.qi4j.library.swing.visualizer.application;

import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public interface SelectionListener
{
    /**
     * Invoked when application node is selected
     *
     * @param aDescriptor The selected application descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onSelected( ApplicationDescriptor aDescriptor );

    /**
     * Invoked when composite node is selected.
     *
     * @param aDescriptor The selected composite descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onSelected( CompositeDescriptor aDescriptor );

    /**
     * Invoked when layer node is selected
     *
     * @param aDescriptor The selected layer descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onSelected( LayerDescriptor aDescriptor );

    /**
     * Invoked when module node is selected.
     *
     * @param aDescriptor The selected module descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onSelected( ModuleDescriptor aDescriptor );
}
