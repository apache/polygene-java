package org.qi4j.library.struts2.example.actions;

import org.apache.struts2.config.Result;
import org.apache.struts2.config.Results;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.library.struts2.example.Item;
import org.qi4j.library.struts2.support.list.ListOf;
import org.qi4j.library.struts2.support.list.ProvidesListOfMixin;

import com.opensymphony.xwork2.Action;

@ListOf(Item.class)
@Results({
    @Result(name="success", value="/jsp/listItems.jsp")
})
@Mixins(ProvidesListOfMixin.class)
public interface ListItems extends Action, Composite {
}
