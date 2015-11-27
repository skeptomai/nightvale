package com.skeptomai;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class MP3Fetcher implements Callable<String> {
    private static final String outputDir = System.getProperty("user.dir");
    private static final Logger log = Logger.getLogger(MP3Fetcher.class.getName());

    private final NetHttpTransport netHttpTransport;
    private final JacksonFactory jsonFactory;

    private String urlToFetch;

    public MP3Fetcher(String url) {
        netHttpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();
        urlToFetch = url;
    }

    public String call() throws IOException, IllegalArgumentException {
        if (urlToFetch == null) {
            throw new IllegalArgumentException("Need a URL to fetch");
        }

        log.info("urlToFetch: " + urlToFetch);
        String[] filenameParts = urlToFetch.split("/");
        String basename = filenameParts[filenameParts.length - 1];
        String absoluteFilePath = outputDir + File.separator + basename;
        File finalMP3 = new File(absoluteFilePath);
        Path pathFinalMP3 = finalMP3.toPath();

        if (finalMP3.exists()) {
            log.info("Looks like " + basename + " is already there.. Skipping");
        } else {
            GenericUrl url = new GenericUrl(urlToFetch);
            HttpRequestFactory requestFactory =
                    netHttpTransport.createRequestFactory((request) -> request.setParser(
                            new JsonObjectParser(jsonFactory)));

            HttpRequest request = requestFactory.buildGetRequest(url);
            InputStream is = request.execute().getContent();
            File f = File.createTempFile("mp3fetcher", ".mp3");
            log.info(f.getAbsolutePath());
            //f.deleteOnExit();

            FileOutputStream outputStream = new FileOutputStream(f);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            Path pathTempMP3 = f.toPath();


            log.info("Moving " + pathTempMP3.toString() + " to " + pathFinalMP3.toString());
            Files.move(pathTempMP3, pathFinalMP3);
        }
        return pathFinalMP3.toString();
    }


}