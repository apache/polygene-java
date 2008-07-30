/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.quikit;

import java.io.Serializable;

public class DisplayInfo
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int order;
    private String label;
    private boolean shownInTable;
    private String defaultValue;
    private boolean defaultDisplayProperty;

    public DisplayInfo( int order )
    {
        this( order, null );
    }

    public DisplayInfo( int order, String label )
    {
        this( order, label, false );
    }

    public DisplayInfo( int order, String label, boolean shownInTable )
    {
        this( order, label, shownInTable, null );
    }

    public DisplayInfo( int order, String label, boolean shownInTable, String defaultValue )
    {
        this( order, label, shownInTable, defaultValue, false );
    }

    public DisplayInfo( int order, String label, boolean shownInTable, String defaultValue, boolean defaultDisplayProperty )
    {
        this.order = order;
        this.label = label;
        this.shownInTable = shownInTable;
        this.defaultValue = defaultValue;
        this.defaultDisplayProperty = defaultDisplayProperty;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public boolean isVisible()
    {
        return shownInTable;
    }

    public int order()
    {
        return order;
    }

    public boolean isDefaultDisplayProperty()
    {
        return defaultDisplayProperty;
    }
}
