package com.networknt.oauth.client.handler;

import com.networknt.client.Client;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.status.Status;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
* Generated by swagger-codegen
*/
public class Oauth2ClientPutHandlerTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(Oauth2ClientPutHandlerTest.class);

    @Test
    public void testOauth2ClientPutHandler() throws ClientException, ApiException, UnsupportedEncodingException {

        String c = "{\"clientId\":\"f7d42348-c647-4efb-a52d-4c5787421e72\",\"clientType\":\"service\",\"clientName\":\"Retail Account\",\"clientDesc\":\"Microservices for Retail Account\",\"scope\":\"act.r act.w\", \"redirectUrl\": \"http://localhost:8080/authorization\", \"ownerId\":\"admin\"}";
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPut httpPut = new HttpPut("http://localhost:6884/oauth2/client");
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setEntity(new StringEntity(c));

        try {
            CloseableHttpResponse response = client.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(200, statusCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOwnerNotFound() throws ClientException, ApiException, UnsupportedEncodingException {

        String c = "{\"clientId\":\"f7d42348-c647-4efb-a52d-4c5787421e72\",\"clientType\":\"service\",\"clientName\":\"Retail Account\",\"clientDesc\":\"Microservices for Retail Account\",\"scope\":\"act.r act.w\", \"redirectUrl\": \"http://localhost:8080/authorization\", \"ownerId\":\"fake\"}";
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPut httpPut = new HttpPut("http://localhost:6884/oauth2/client");
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setEntity(new StringEntity(c));

        try {
            CloseableHttpResponse response = client.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(404, statusCode);
            if(statusCode == 404) {
                Status status = Config.getInstance().getMapper().readValue(body, Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12013", status.getCode());
                Assert.assertEquals("USER_NOT_FOUND", status.getMessage()); // response_type missing
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}