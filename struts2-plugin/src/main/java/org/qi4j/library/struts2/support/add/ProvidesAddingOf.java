package org.qi4j.library.struts2.support.add;

import com.opensymphony.xwork2.Preparable;
import org.qi4j.library.struts2.support.HasInput;
import org.qi4j.library.struts2.support.StrutsAction;

public interface ProvidesAddingOf<T>
    extends Preparable, HasInput, StrutsAction
{
    T getState();
}
