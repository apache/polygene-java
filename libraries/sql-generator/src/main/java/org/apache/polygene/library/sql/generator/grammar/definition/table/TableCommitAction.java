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
 *
 *
 */
package org.apache.polygene.library.sql.generator.grammar.definition.table;

/**
 * This enum is for table commit action ({@code ON COMMIT} ...) in table definition. The commit action may be either
 * {@link #ON_COMMIT_PRESERVE_ROWS} or {@link #ON_COMMIT_DELETE_ROWS}.
 *
 * @see TableDefinition
 */
public final class TableCommitAction
{

    /**
     * The commit action for {@code ON COMMIT PRESERVE ROWS}.
     */
    public static final TableCommitAction ON_COMMIT_PRESERVE_ROWS = new TableCommitAction();

    /**
     * The commit action for {@code ON COMMIT DELETE ROWS}.
     */
    public static final TableCommitAction ON_COMMIT_DELETE_ROWS = new TableCommitAction();
}
