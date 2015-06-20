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
package org.qi4j.library.struts2.example.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.config.Result;
import org.apache.struts2.config.Results;
import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.struts2.example.Item;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;
import org.qi4j.library.struts2.support.edit.ProvidesEditingOf;
import org.qi4j.library.struts2.support.edit.ProvidesEditingOfMixin;

@Results( {
    @Result( name = "input", value = "/jsp/editItem.jsp" ),
    @Result( name = "error", value = "/jsp/editItem.jsp" ),
    @Result( name = "success", value = "listItems", type = ServletActionRedirectResult.class )
} )
@Mixins( { ProvidesEditingOfMixin.class, ProvidesEntityOfMixin.class, ActionSupport.class } )
public interface EditItem
    extends ProvidesEditingOf<Item>, Composite
{
}
