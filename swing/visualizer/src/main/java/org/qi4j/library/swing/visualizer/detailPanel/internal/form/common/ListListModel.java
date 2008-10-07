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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.common;

import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ListListModel<T> extends AbstractListModel
{
    private static final long serialVersionUID = 1L;

    public static ListListModel EMPTY_MODEL = new ListListModel( Collections.emptyList() );

    private final List<T> elements;

    public ListListModel( List<T> contents )
    {
        if( contents == null )
        {
            contents = Collections.emptyList();
        }

        elements = contents;
    }

    public final int getSize()
    {
        return elements.size();
    }

    public final T getElementAt( int index )
    {
        return elements.get( index );
    }
}
