/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.manual.recipes.properties;

import java.awt.Rectangle;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.zest.api.Qi4j;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.property.Property;

public class SwingPanel
{
    private static final Rectangle SIZE_32_32 = new Rectangle( 32, 32 );
    private Locale locale;

// START SNIPPET: info-use
    @Structure
    private Qi4j api;
// END SNIPPET: info-use

// START SNIPPET: info-use
    private void addProperty( JPanel panel, Property<?> property )
    {
        SwingInfo info = api.propertyDescriptorFor( property ).metaInfo( SwingInfo.class );
        Icon icon = info.icon( SIZE_32_32 );
        panel.add(  new JLabel(info.displayName( this.locale ), icon, JLabel.CENTER) );
    }
// START SNIPPET: info-use
}
