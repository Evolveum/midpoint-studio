package com.evolveum.midpoint.studio.impl.client;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface MessageListener {

    enum MessageType {

        REQUEST, RESPONSE, FAULT;
    }

    void handleMessage(String messageId, MessageType type, String message);
}
