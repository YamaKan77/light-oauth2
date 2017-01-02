package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Oauth2ClientPutHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPutHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String clientId = (String)client.get("clientId");

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            // make sure the owner_id exists in users map.
            String ownerId = (String)client.get("ownerId");
            if(ownerId != null) {
                IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    Status status = new Status(USER_NOT_FOUND, ownerId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                }
            }
            clients.set(clientId, client);
        }
    }
}
