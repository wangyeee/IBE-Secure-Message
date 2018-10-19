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
| server/key_dist_server  |A Springboot application to distributes encryption keys. It talks to the key_gen_server via REST API calls with IBE encrypted payload.|
| server/key_gen_server   |A Springboot application to generate encryption keys.|
| client/iOS              |An iOS app for user to send and receive messages.|
