/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.serialization.javaxxml;

import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.spi.serialization.XmlSerialization;

/**
 * javax.xml state serialization.
 *
 * The XML representations consumed and produced by this service are, by default, verbose, and safe to deserialize.
 * This is because the default mapping is purely structural.
 * You can customize the XML representations using {@link JavaxXmlSettings} and {@link JavaxXmlAdapters}.
 *
 * The following describe how state is represented by default.
 *
 * Because a valid XML document can only contain a single node and it must be an element, all
 * {@link org.w3c.dom.Document}s have a root element {@literal &lt;state/&gt;}. This serialization implementation
 * also impose that the root element can only contain a single node, of any type.
 *
 * {@literal null} is represented as {@literal &lt;null/&gt;}.
 * Plain values are represented as {@link org.w3c.dom.Text} nodes.
 * Iterables and Streams are represented as {@literal &lt;collection/&gt;} {@link org.w3c.dom.Element}s.
 * Maps are represented as {@literal &lt;dictionary/&gt;} {@link org.w3c.dom.Element}s.
 *
 * This is how a {@literal null} plain value is represented: {@literal &lt;state&gt;&lt;null/&gt;&lt;/state&gt;}.
 * And a plain {@literal LocalDate}: {@literal &lt;state&gt;2017-01-01&lt;/state&gt;}
 *
 * This is how a fictional value including a collection and a map is represented:
 * <code>
 *     &lt;state&gt;
 *         &lt;stringProperty&gt;and it's value&lt;/stringProperty&gt;
 *         &lt;bigDecimalProperty&gt;4.22376931348623157E+310&lt;/bigDecimalProperty&gt;
 *         &lt;nullProperty&gt;&lt;null/&gt;&lt;/nullProperty&gt;
 *         &lt;booleanProperty&gt;false&lt;/booleanProperty&gt;
 *         &lt;stringCollectionProperty&gt;
 *             &lt;collection&gt;
 *                  item1
 *                  item2 &lt;!-- As multiple text nodes --&gt;
 *             &lt;/collection&gt;
 *         &lt;/stringCollectionProperty&gt;
 *         &lt;mapProperty&gt;
 *             &lt;map&gt;
 *                 &lt;foo&gt;bar&lt;/foo&gt;
 *                 &lt;bazar&gt;cathedral&lt;/bazar&gt;
 *             &lt;/map&gt;
 *         &lt;/mapProperty&gt;
 *         &lt;complexKeyMapProperty&gt;
 *             &lt;map&gt;
 *                 &lt;entry&gt;
 *                     &lt;key&gt;
 *                         &lt;foo&gt;bar&lt;/foo&gt;
 *                         &lt;bazar&gt;cathedral&lt;/bazar&gt;
 *                     &lt;/key&gt;
 *                     &lt;value&gt;23&lt;/value&gt;
 *                 &lt;/entry&gt;
 *                 &lt;entry&gt;
 *                     &lt;key&gt;
 *                         &lt;foo&gt;baz&lt;/foo&gt;
 *                         &lt;bazar&gt;bar&lt;/bazar&gt;
 *                     &lt;/key&gt;
 *                     &lt;value&gt;42&lt;/value&gt;
 *                 &lt;/entry&gt;
 *             &lt;/map&gt;
 *         &lt;/complexKeyMapProperty&gt;
 *     &lt;/state&gt;
 * </code>
 *
 */
@Mixins( { JavaxXmlSerializer.class, JavaxXmlDeserializer.class } )
public interface JavaxXmlSerialization extends XmlSerialization
{
}
