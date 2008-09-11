<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>Item List</title>
</head>
<body>
Items:
<ul>
    <s:iterator value="list().iterator()">
        <li>
            <s:property value="name()"/>
            <a href="<s:url action="editItem" method="input"><s:param name="id" value="identity().get()" /></s:url>">Edit</a>
        </li>
    </s:iterator>
</ul>
<a href="<s:url action="addItem" method="input" />">Add</a>
</body>
</html>

