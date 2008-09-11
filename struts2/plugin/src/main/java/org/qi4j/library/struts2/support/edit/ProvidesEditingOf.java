package org.qi4j.library.struts2.support.edit;

import org.qi4j.library.struts2.support.HasInput;
import org.qi4j.library.struts2.support.ProvidesEntityOf;
import org.qi4j.library.struts2.support.StrutsAction;

import com.opensymphony.xwork2.Preparable;

public interface ProvidesEditingOf<T> extends ProvidesEntityOf<T>, Preparable, HasInput, StrutsAction {
    void prepareInput() throws Exception;
}
