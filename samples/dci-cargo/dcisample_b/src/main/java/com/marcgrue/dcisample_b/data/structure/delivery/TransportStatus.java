package com.marcgrue.dcisample_b.data.structure.delivery;

/**
 * TransportStatus
 *
 * A transport status represents what transportation state a cargo is in.
 */
public enum TransportStatus
{
    NOT_RECEIVED,
    IN_PORT,
    ONBOARD_CARRIER,
    CLAIMED,
    UNKNOWN
}
