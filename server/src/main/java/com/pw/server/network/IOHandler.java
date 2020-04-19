package com.pw.server.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class IOHandler {
    PrintWriter getWriter(OutputStream outputStream) {
        return new PrintWriter(outputStream, true);
    }

    BufferedReader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream));
    }
}
