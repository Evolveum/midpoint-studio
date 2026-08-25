package com.evolveum.midpoint.studio.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Service.getLog / getLogFileSize against a mock midPoint /ws/rest/log endpoint.
 */
public class ServiceLogTest {

    private MockWebServer server;

    private Service service;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        service = new ServiceFactory()
                .url(server.url("/").toString().replaceAll("/$", ""))
                .create();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void getLogSendsParamsAndParsesHeaders() throws Exception {
        byte[] body = "line1\nline2\n".getBytes(StandardCharsets.UTF_8);
        Buffer buffer = new Buffer().write(body);
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/plain")
                .addHeader("ReturnedDataPosition", "100")
                .addHeader("ReturnedDataComplete", "false")
                .addHeader("CurrentLogFileSize", "5000")
                .setBody(buffer));

        LogFileContent content = service.getLog(100L, 12L);

        RecordedRequest request = server.takeRequest();
        assertEquals("/ws/rest/log", request.getRequestUrl().encodedPath());
        assertEquals("100", request.getRequestUrl().queryParameter("fromPosition"));
        assertEquals("12", request.getRequestUrl().queryParameter("maxSize"));
        // Accept must be text/plain and must not also carry application/xml
        assertEquals(1, request.getHeaders().values("Accept").size());
        assertEquals("text/plain", request.getHeader("Accept"));

        assertArrayEquals(body, content.getContent());
        assertEquals(100L, content.getAt());
        assertFalse(content.isComplete());
        assertEquals(5000L, content.getLogFileSize());
    }

    @Test
    void getLogNegativeFromPositionIsSentVerbatim() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("ReturnedDataPosition", "4936")
                .addHeader("ReturnedDataComplete", "true")
                .addHeader("CurrentLogFileSize", "5000")
                .setBody("tail"));

        service.getLog(-64L, 1024L);

        RecordedRequest request = server.takeRequest();
        assertEquals("-64", request.getRequestUrl().queryParameter("fromPosition"));
    }

    @Test
    void getLogUnauthorizedThrowsAuthenticationException() {
        server.enqueue(new MockResponse().setResponseCode(401));

        assertThrows(AuthenticationException.class, () -> service.getLog(0L, 100L));
    }

    @Test
    void getLogFileSizeParsesPlainTextLong() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/plain")
                .setBody("123456"));

        assertEquals(123456L, service.getLogFileSize());

        RecordedRequest request = server.takeRequest();
        assertEquals("/ws/rest/log/size", request.getRequestUrl().encodedPath());
    }
}
