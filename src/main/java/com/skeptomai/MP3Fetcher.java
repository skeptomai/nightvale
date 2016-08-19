package com.skeptomai;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.util.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class MP3Fetcher implements Callable<String> {
    private final String outputDir;
    private static final Logger log = Logger.getLogger(MP3Fetcher.class.getName());

    private final HttpRequestFactory requestFactory;

    private String urlToFetch;

    public MP3Fetcher(String s, String outputDir,
                      HttpRequestFactory requestFactory) {
        urlToFetch = s;
        this.outputDir = outputDir;
        this.requestFactory = requestFactory;
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

            HttpRequest request = requestFactory.buildGetRequest(url);
            File f = downloadToTempFile(request);
            Path pathTempMP3 = f.toPath();
            moveToFile(pathTempMP3, pathFinalMP3);
        }

        return pathFinalMP3.toString();
    }

    static private File downloadToTempFile(HttpRequest request) throws IOException {
        File f = File.createTempFile("mp3fetcher", ".mp3");
        log.info(f.getAbsolutePath());
        try(InputStream is = request.execute().getContent();
            FileOutputStream outputStream = new FileOutputStream(f)) {
            long copy = ByteStreams.copy(is, outputStream);
            log.info("copying " + String.valueOf(copy) + " bytes");
        }
        return f;
    }

    static private void moveToFile(Path inPath, Path outPath) throws IOException {
        log.info("Moving " + inPath.toString() + " to " + outPath.toString());
        Files.move(inPath, outPath, ATOMIC_MOVE);
    }


}