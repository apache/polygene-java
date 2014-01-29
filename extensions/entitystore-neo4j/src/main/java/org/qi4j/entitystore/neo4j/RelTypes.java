package org.qi4j.entitystore.neo4j;

import org.neo4j.graphdb.RelationshipType;

enum RelTypes
    implements RelationshipType
{
    ENTITY_TYPE_REF,
    IS_OF_TYPE,
    MANY_ASSOCIATION,
    NAMED_ASSOCIATION
}
