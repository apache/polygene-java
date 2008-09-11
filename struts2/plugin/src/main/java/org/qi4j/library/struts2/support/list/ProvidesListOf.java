package org.qi4j.library.struts2.support.list;

import org.qi4j.library.struts2.support.StrutsAction;

import com.opensymphony.xwork2.Preparable;

public interface ProvidesListOf<T> extends Preparable, StrutsAction {
    Iterable<T> list();
}
