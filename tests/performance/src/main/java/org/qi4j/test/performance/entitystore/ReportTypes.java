/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.performance.entitystore;

public interface ReportTypes
{
    String CREATE_ENTITY_WITH_SINGLE_PROPERTY = "createEntityWithSingleProperty";
    String CREATE_ENTITY_WITH_ONLY_PROPERTIES = "createEntityWithOnlyProperties";
    String CREATE_ENTITY_WITH_SINGLE_ASSOCIATION = "createEntityWithSingleAssociation";
    String CREATE_ENTITY_WITH_SEVERAL_ASSOCIATIONS = "createEntityWithSeveralAssociations";
    String CREATE_ENTITY_WITH_SINGLE_MANYASSOCIATION = "createEntityWithSingleManyAssociations";
    String CREATE_ENTITY_WITH_SEVERAL_MANYASSOCIATIONS = "createEntityWithSeveralManyAssociations";
    String CREATE_ENTITY_WITH_MIXED_COMPOSITION = "createEntityWithMixedComposition";

    String READ_ENTITY_WITH_SINGLE_PROPERTY = "readEntityWithSingleProperty";
    String READ_ENTITY_WITH_ONLY_PROPERTIES = "readEntityWithOnlyProperties";
    String READ_ENTITY_WITH_SINGLE_ASSOCIATION = "readEntityWithSingleAssociation";
    String READ_ENTITY_WITH_SEVERAL_ASSOCIATIONS = "readEntityWithSeveralAssociations";
    String READ_ENTITY_WITH_SINGLE_MANYASSOCIATION = "readEntityWithSingleManyAssociations";
    String READ_ENTITY_WITH_SEVERAL_MANYASSOCIATIONS = "readEntityWithSeveralManyAssociations";
    String READ_ENTITY_WITH_MIXED_COMPOSITION = "readEntityWithMixedComposition";

    String TRAVERSE_ONE_LEVEL = "traverseOneLevel";
    String TRAVERSE_TWO_LEVEL = "traverseTwoLevel";
    String TRAVERSE_THREE_LEVEL = "traverseThreeLevel";
    String TRAVERSE_FIVE_LEVEL = "traverseFiveLevel";
}
