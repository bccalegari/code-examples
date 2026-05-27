package br.com.brunodacostacalegari.httpconnector.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String queryString;
    private final String protocol;

    private final HttpHeaders headers;
    private final Map<String, List<String>> queryParams;

    private final byte[] body;

    private final String remoteAddress;
    private final int remotePort;

    public HttpRequest(
            String method, String path, String queryString, String protocol,
            HttpHeaders headers, Map<String, List<String>> queryParams,
            byte[] body, String remoteAddress, int remotePort
    ) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.protocol = protocol;
        this.headers = headers;
        this.queryParams = Map.copyOf(queryParams);
        this.body = body == null ? new byte[0] : body.clone();
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return (values == null || values.isEmpty()) ? null : values.getFirst();
    }

    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    public Set<String> getHeaderNames() {
        return headers.names();
    }

    public String getQueryParameter(String name) {
        List<String> values = queryParams.get(name);
        return (values == null || values.isEmpty()) ? null : values.getFirst();
    }

    public List<String> getQueryParameters(String name) {
        return List.copyOf(queryParams.getOrDefault(name, List.of()));
    }

    public List<String> getQueryParameterNames() {
        return List.copyOf(queryParams.keySet());
    }

    public Map<String, List<String>> getAllQueryParameters() {
        return Map.copyOf(queryParams);
    }

    public InputStream getBodyInputStream() {
        return new ByteArrayInputStream(body);
    }

    public int getContentLength() {
        return body.length;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }
}