IBE-Secure-Message
==================

Instant Message server and client with IBE(Identity-Based Encryption) to protect message

# List of components

| Path | Comments |
| ------------------------|------|
| jlib/ibejnilib          |The native Java library. |
| jlib/jibe               |OO encapsulation of the native library. |
| nativelib               |Implemenation of the native library. |
| server/msg_relay_server |A PHP application handling user messages. |
| server/key_dist_server<sup>*</sup>  |A EJB client project distributes encryption keys.|
| server/key_gen_server<sup>*</sup>   |A EJB project generating encryption keys.|
| client/iOS              |An iOS app for user to send and receive messages.|

\* This is considered to be obsolete since most of libraries used are out of date.
