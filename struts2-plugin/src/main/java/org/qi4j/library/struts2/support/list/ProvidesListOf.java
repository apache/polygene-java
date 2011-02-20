package org.qi4j.library.struts2.support.list;

import com.opensymphony.xwork2.Preparable;
import org.qi4j.library.struts2.support.StrutsAction;

public interface ProvidesListOf<T>
    extends Preparable, StrutsAction
{
    Iterable<T> list();
}
