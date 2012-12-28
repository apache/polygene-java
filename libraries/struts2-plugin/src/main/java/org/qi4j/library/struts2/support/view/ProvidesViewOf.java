package org.qi4j.library.struts2.support.view;

import org.qi4j.library.struts2.support.ProvidesEntityOf;
import org.qi4j.library.struts2.support.StrutsAction;

public interface ProvidesViewOf<T>
    extends ProvidesEntityOf<T>, StrutsAction
{
}
