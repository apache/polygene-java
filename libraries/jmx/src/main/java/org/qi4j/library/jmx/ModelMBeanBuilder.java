/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.jmx;

import java.util.ArrayList;
import java.util.List;
import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.modelmbean.*;

/**
 * Helper builder for ModelMBeans
 */
public class ModelMBeanBuilder
{
    List<ModelMBeanAttributeInfo> attributes = new ArrayList<ModelMBeanAttributeInfo>();
    List<ModelMBeanConstructorInfo> constructors = new ArrayList<ModelMBeanConstructorInfo>();
    List<ModelMBeanOperationInfo> operations = new ArrayList<ModelMBeanOperationInfo>();
    List<ModelMBeanNotificationInfo> notifications = new ArrayList<ModelMBeanNotificationInfo>();

    ObjectName objectName;
    String displayName;
    private final String className;

    public ModelMBeanBuilder( ObjectName objectName, String displayName, String className )
    {
        this.objectName = objectName;
        this.displayName = displayName;
        this.className = className;
    }

    public ModelMBeanBuilder attribute( String name, String displayName, String type, String description, String getMethod, String setMethod )
    {
        Descriptor stateDesc = new DescriptorSupport();
        stateDesc.setField( "name", name );
        stateDesc.setField( "descriptorType", "attribute" );
        stateDesc.setField( "displayName", displayName );
        if (getMethod != null)
        {
            stateDesc.setField( "getMethod", getMethod );

            operation( getMethod, description, type, ModelMBeanOperationInfo.INFO );
        }

        if (setMethod != null)
        {
            stateDesc.setField( "setMethod", setMethod );
            operation( setMethod, description, type, ModelMBeanOperationInfo.INFO, new MBeanParameterInfo("Value", type, description) );
        }

        ModelMBeanAttributeInfo attributeInfo = new ModelMBeanAttributeInfo(
                name,
                type,
                description,
                getMethod != null,
                setMethod != null,
                getMethod != null && getMethod.startsWith( "is" ),
                stateDesc );
        attributes.add( attributeInfo );

        return this;
    }

    public ModelMBeanAttributeInfo getAttribute(String name)
    {
        for (ModelMBeanAttributeInfo attribute : attributes)
        {
            if (attribute.getName().equals(name))
                return attribute;
        }

        return null;
    }

    public ModelMBeanBuilder operation( String name, String description, String returnType, int impact, MBeanParameterInfo... parameters)
    {
        Descriptor stateDesc = new DescriptorSupport();
        stateDesc.setField( "name", name );
        stateDesc.setField( "descriptorType", "operation" );
        stateDesc.setField( "class", className );
        stateDesc.setField( "role", "operation" );
        stateDesc.setField( "targetType", "objectReference" );

        ModelMBeanOperationInfo operationInfo = new ModelMBeanOperationInfo(
                name,
                description,
                parameters,
                returnType,
                impact,
                stateDesc);
        operations.add( operationInfo );

        return this;
    }

    public RequiredModelMBean newModelMBean() throws MBeanException
    {
        Descriptor mmbDesc = new DescriptorSupport();
        mmbDesc.setField( "name", objectName.toString() );
        mmbDesc.setField( "descriptorType", "mbean" );
        mmbDesc.setField( "displayName", displayName );

        ModelMBeanInfo modelMBeanInfo = new ModelMBeanInfoSupport(
                className,
                displayName,
                attributes.toArray( new ModelMBeanAttributeInfo[attributes.size()]),
                constructors.toArray( new ModelMBeanConstructorInfo[constructors.size()]),
                operations.toArray( new ModelMBeanOperationInfo[operations.size()]),
                notifications.toArray( new ModelMBeanNotificationInfo[notifications.size()]) );

        modelMBeanInfo.setMBeanDescriptor( mmbDesc );

        return new RequiredModelMBean(modelMBeanInfo);
    }
}
