<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<!DOCTYPE html PUBLIC
"-//W3C//DTD XHTML 1.1 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <title><decorator:title default="Struts Starter"/></title>
    <link href="<s:url value='/styles/main.css'/>" rel="stylesheet" type="text/css" media="all"/>
    <link href="<s:url value='/struts/niftycorners/niftyCorners.css'/>" rel="stylesheet" type="text/css"/>
    <link href="<s:url value='/struts/niftycorners/niftyPrint.css'/>" rel="stylesheet" type="text/css" media="print"/>
    <decorator:head/>
</head>
<body id="page-home">
<div id="page">
    <div id="header" class="clearfix">
        HEADER
        <hr/>
    </div>

    <div id="content" class="clearfix">
        <div id="main">
            <h3>Main Content</h3>
            <decorator:body/>
            <hr/>
        </div>

        <div id="sub">
            <h3>Sub Content</h3>
        </div>


        <div id="local">
            <h3>Local Nav. Bar</h3>
            <ul>
                <li><a href="#">Content page 1</a></li>
                <li><a href="#">Content page 2</a></li>
                <li><a href="#">Content page 3</a></li>
                <li><a href="#">Content page 4</a></li>
                <li><a href="#">Content page 5</a></li>
                <li><a href="#">Content page 6</a></li>
            </ul>
        </div>


        <div id="nav">
            <div class="wrapper">
                <h3>Nav. bar</h3>
                <ul class="clearfix">
                    <li><a href="#">Menu 1</a></li>
                    <li><a href="#">Menu 2</a></li>
                    <li><a href="#">Menu 3</a></li>
                    <li><a href="#">Menu 4</a></li>
                    <li><a href="#">Menu 5</a></li>
                    <li class="last"><a href="#">Menu 6</a></li>
                </ul>
            </div>
            <hr/>
        </div>
    </div>

    <div id="footer" class="clearfix">
        Footer
    </div>

</div>

<div id="extra1">&nbsp;</div>
<div id="extra2">&nbsp;</div>
</body>
</html>
