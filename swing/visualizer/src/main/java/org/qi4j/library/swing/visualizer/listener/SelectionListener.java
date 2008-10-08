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
package org.qi4j.library.swing.visualizer.listener;

import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MixinDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public interface SelectionListener
{
    /**
     * Invoked when an application node is selected.
     *
     * @param aDescriptor The selected application descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onApplicationSelected( ApplicationDetailDescriptor aDescriptor );

    /**
     * Invoked when a layer node is selected.
     *
     * @param aDescriptor The selected layer descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onLayerSelected( LayerDetailDescriptor aDescriptor );

    /**
     * Invoked when a module node is selected.
     *
     * @param aDescriptor The selected module descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onModuleSelected( ModuleDetailDescriptor aDescriptor );

    /**
     * Invoked when a composite node is selected.
     *
     * @param aDescriptor The selected composite descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onCompositeSelected( CompositeDetailDescriptor aDescriptor );

    /**
     * Invoked when a composite node is selected.
     *
     * @param aDescriptor The selected entity descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onEntitySelected( EntityDetailDescriptor aDescriptor );

    /**
     * Invoked when a service node is selected.
     *
     * @param aDescriptor The selected service descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onServiceSelected( ServiceDetailDescriptor aDescriptor );

    /**
     * Invoked when an object node is selected.
     *
     * @param aDescriptor The selected object descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onObjectSelected( ObjectDetailDescriptor aDescriptor );

    /**
     * Invoked when a mixin node is selected.
     *
     * @param aDescriptor The selected mixin descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    void onMixinSelected( MixinDetailDescriptor aDescriptor );

    /**
     * Invoked when the component reset selection.
     *
     * @since 0.5
     */
    void resetSelection();
}
