/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.sql.datasource;

import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public interface DataSourceServiceComposite
        extends DataSourceService, Activatable, ServiceComposite
{
}
