package com.networknt.oauth.key.handler;

import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;

public class Oauth2KeyKeyIdGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2KeyKeyIdGetHandler.class);

    static final String CONFIG_SECURITY = "security";
    static final String CONFIG_JWT = "jwt";
    static final String CONFIG_CERTIFICATE = "certificate";
    static final String KEY_NOT_FOUND = "ERR12017";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String RUNTIME_EXCEPTION = "ERR10010";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String SQL_EXCEPTION = "ERR10017";

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // check if client_id and client_secret in header are valid pair.
        HeaderValues values = exchange.getRequestHeaders().get(AUTHORIZATION);
        String authHeader;
        if(values != null) {
            authHeader = values.getFirst();
        } else {
            Status status = new Status(MISSING_AUTHORIZATION_HEADER);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        if(authHeader == null) {
            Status status = new Status(MISSING_AUTHORIZATION_HEADER);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        String clientId = authenticate(authHeader);
        if(clientId != null) {
            if(logger.isDebugEnabled()) logger.debug("clientId = " + clientId);

            String keyId = exchange.getQueryParameters().get("keyId").getFirst();
            if(logger.isDebugEnabled()) logger.debug("keyId = " + keyId);
            // find the location of the certificate
            Map<String, Object> config = Config.getInstance().getJsonMapConfig(CONFIG_SECURITY);
            Map<String, Object> jwtConfig = (Map<String, Object>)config.get(CONFIG_JWT);
            Map<String, Object> certificateConfig = (Map<String, Object>)jwtConfig.get(CONFIG_CERTIFICATE);
            // find the path for certificate file
            String filename = (String)certificateConfig.get(keyId);
            String content = Config.getInstance().getStringFromFile(filename);
            if(logger.isDebugEnabled()) logger.debug("certificate = " + content);
            if(content != null) {
                exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/text");
                exchange.getResponseSender().send(content);
            } else {
                logger.info("Certificate " + Encode.forJava(filename) + " not found.");
                Status status = new Status(KEY_NOT_FOUND, keyId);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String authenticate(String authHeader) throws ApiException {
        String result = null;
        if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {
            String base64Challenge = authHeader.substring(PREFIX_LENGTH);
            String plainChallenge;
            try {
                ByteBuffer decode = FlexBase64.decode(base64Challenge);
                // assume charset is UTF_8
                Charset charset = StandardCharsets.UTF_8;
                plainChallenge = new String(decode.array(), decode.arrayOffset(), decode.limit(), charset);
                logger.debug("Found basic auth header %s (decoded using charset %s) in %s", plainChallenge, charset, authHeader);
                int colonPos;
                if ((colonPos = plainChallenge.indexOf(COLON)) > -1) {
                    String clientId = plainChallenge.substring(0, colonPos);
                    String clientSecret = plainChallenge.substring(colonPos + 1);
                    // match with db/cached user credentials.
                    Map<String, Object> client = (Map<String, Object>) CacheStartupHookProvider.hz.getMap("clients").get(clientId);
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    }
                    if(!clientSecret.equals(client.get("clientSecret"))) {
                        throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                    }
                    result = clientId;
                }
            } catch (IOException e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(RUNTIME_EXCEPTION));
            }
        }
        return result;
    }
}
