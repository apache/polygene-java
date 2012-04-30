package com.marcgrue.dcisample_a.data.shipping.delivery;

/**
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
