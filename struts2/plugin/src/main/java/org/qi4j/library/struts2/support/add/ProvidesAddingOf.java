package org.qi4j.library.struts2.support.add;

import org.qi4j.library.struts2.support.HasInput;
import org.qi4j.library.struts2.support.StrutsAction;

import com.opensymphony.xwork2.Preparable;

public interface ProvidesAddingOf<T> extends Preparable, HasInput, StrutsAction {
    T getState();
}
