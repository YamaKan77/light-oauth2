package com.networknt.oauth.code.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CodeHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(CodeHandler.class);
    static final String INVALID_CODE_REQUEST = "ERR12009";
    static final String INVALID_RESPONSE_TYPE = "ERR12010";
    static final String CLIENT_ID_NOTFOUND = "ERR12006";

    // this singleton map contains all the auth codes generated. if the code is
    // used in TokenHandler, then it is removed.
    public static Map<String, Object> codes = new ConcurrentHashMap<String, Object>();
    public static final Map<String, Object> clients = Config.getInstance().getJsonMapConfig("client");

    private final ObjectMapper objectMapper;

    public CodeHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(
                Headers.CONTENT_TYPE, "application/json");

        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<String, String>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        //logger.debug("params", params);
        String responseType = params.get("response_type");
        String clientId = params.get("client_id");
        if(responseType == null || clientId == null) {
            Status status = new Status(INVALID_CODE_REQUEST);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            if(!"code".equals(responseType)) {
                Status status = new Status(INVALID_RESPONSE_TYPE, responseType);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
            } else {
                // check if the client_id is valid
                Map<String, Object> cMap = (Map<String, Object>)clients.get(clientId);
                if(cMap == null) {
                    Status status = new Status(CLIENT_ID_NOTFOUND, clientId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                } else {
                    // generate auth code
                    String code = Util.getUUID();
                    final SecurityContext context = exchange.getSecurityContext();
                    String userId = context.getAuthenticatedAccount().getPrincipal().getName();
                    codes.put(code, userId);
                    String redirectUri = params.get("redirect_uri");
                    if(redirectUri == null) {
                        redirectUri = (String)cMap.get("redirect_uri");
                    }
                    redirectUri = redirectUri + "?code=" + code;
                    // now redirect here.
                    exchange.setStatusCode(StatusCodes.FOUND);
                    exchange.getResponseHeaders().put(Headers.LOCATION, redirectUri);
                    exchange.endExchange();
                }
            }
        }
    }
}
