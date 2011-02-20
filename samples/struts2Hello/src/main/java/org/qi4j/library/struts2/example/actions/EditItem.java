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
