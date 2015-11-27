package com.skeptomai;

import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import org.apache.commons.io.IOUtils;

/**
 * Created by cb on 11/8/15.
 */
public class URLListFetcher implements Callable<String> {

    private final NetHttpTransport netHttpTransport = new NetHttpTransport();
    private final String urlToFetch;

    public URLListFetcher(String url) {
        urlToFetch = url;
    }

    public String call() throws IOException {
        GenericUrl url = new GenericUrl(urlToFetch);
        HttpRequestFactory requestFactory =
                netHttpTransport.createRequestFactory();
        HttpRequest request = requestFactory.buildGetRequest(url);
        StringWriter sw = new StringWriter();
        IOUtils.copy(request.execute().getContent(), sw, StandardCharsets.UTF_8);
        return sw.toString();
    }

}
