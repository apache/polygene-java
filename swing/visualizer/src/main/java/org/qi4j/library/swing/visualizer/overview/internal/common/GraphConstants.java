/*
 * Copyright 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.overview.internal.common;

/**
 * @author Sonny Gill
 */
public class GraphConstants
{
    public static final int PADDING_TOP = 10;
    public static final int PADDING_LEFT = 10;

    public static final int PADDING_RIGHT = 10;
    public static final int PADDING_BOTTOM = 10;

    public static final int hSpace = 10;
    public static final int vSpace = 10;

    public static final String FIELD_NAME = "name";

    /**
     * @see NodeType
     */
    public static final String FIELD_TYPE = "type";

    public static final String FIELD_LAYER_LEVEL = "layer_level";
    public static final String FIELD_USED_LAYERS = "used_layers";
    public static final String FIELD_USED_BY_LAYERS = "used_by_layers";
    public static final String FIELD_DESCRIPTOR = "fieldDescriptors";

    public static final String GROUP_NAME_SERVICES = "Services";
    public static final String GROUP_NAME_ENTITIES = "Entities";
    public static final String GROUP_NAME_COMPOSITES = "Composites";
    public static final String GROUP_NAME_OBJECTS = "Objects";

    private GraphConstants()
    {
    }

}
