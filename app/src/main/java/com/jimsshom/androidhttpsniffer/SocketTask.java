package com.jimsshom.androidhttpsniffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class SocketTask implements Runnable {
    private Socket source;
    private Socket dest;
    private BufferedReader sourceInput;
    private BufferedWriter sourceOutput;
    private boolean isInSSLSession = false;

    public SocketTask(Socket source) throws IOException {
        this.source = source;
        sourceInput = new BufferedReader(new InputStreamReader(source.getInputStream(), "utf8"));
        sourceOutput = new BufferedWriter(new OutputStreamWriter(source.getOutputStream(), "utf8"));
    }

    @Override
    public void run() {
        print("new socket:==========");
        print(source.getRemoteSocketAddress());
        print(source.getInetAddress());
        try {
            source.setKeepAlive(true);
            print(source.getKeepAlive());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        print("=============");

        try {
            way1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void way1() {
        while (true) {
            if (isInSSLSession) {
                break;
            }

            String[] inputs = new String[0];
            try {
                inputs = readPlainInputFromSource();
            } catch (Exception e) {
                printImportant("read exception");
                try {
                    source.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (inputs.length == 0) {
                return;
            }
            String header = inputs[0];
            if (header.startsWith("CONNECT ")) {
                doConnect(inputs);
                continue;
            }
            doHttpProxy(inputs);
        }
    }

    private void doHttpProxy(String[] inputs) {
        String[] headerSegments = inputs[0].split(" ");
        int port = 80;
        // port
        if (headerSegments[1].startsWith("http://")) {
            port = 80;
        } else {
            print("unknown port");
        }
        //host
        String host = "";
        for (String input : inputs) {
            if (input.startsWith("Host: ")) {
                host = input.split(" ")[1];
                break;
            }
        }
        printImportant(inputs[0]);
        print(host + ":" + port);

        Socket dest = null;
        try {
            dest = new Socket(host, port);
            BufferedReader destInput = new BufferedReader(new InputStreamReader(dest.getInputStream(), "utf8"));
            BufferedWriter destOutput = new BufferedWriter(new OutputStreamWriter(dest.getOutputStream(), "utf8"));

            for (String input : inputs) {
                destOutput.write(input + '\n');
            }
            destOutput.newLine();
            destOutput.flush();

            while (!destInput.ready()) {
                Thread.sleep(1000);
                print("http destInput");
            }
            while (true) {
                String s = destInput.readLine();
                if (s == null || s.isEmpty()) {
                    System.out.println("dest end");
                    break;
                }
                print(s);
                sourceOutput.write(s + "\n");
            }
            sourceOutput.newLine();
            sourceOutput.flush();
            print("doHttpProxy end");
            dest.close();
        } catch (Exception e) {
            printImportant("error: " + host + ":" + port);
        }
    }

    private void doConnect(String[] inputs) {
        String[] headerSegments = inputs[0].split(" ");
        int port = Integer.valueOf(headerSegments[1].split(":")[1]);
        String host = headerSegments[1].split(":")[0];

        printImportant("CONNECT: " + host + ":" + port);
        print("CONNECT: " + host + ":" + port);

        try {
            dest = new Socket(host, port);
            print("connect success");
            sourceOutput.write("HTTP/1.1 200 Connection Established\n");
            sourceOutput.newLine();
            sourceOutput.flush();
        } catch (Exception e) {
            printImportant("error: " + host + ":" + port);
            try {
                source.close();
                dest.close();
                return;
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }

        Thread thread1 = new Thread(new TunnelTask(host + "/S", source, dest));
        Thread thread2 = new Thread(new TunnelTask(host + "/D", dest, source));
        thread1.start();
        thread2.start();
        isInSSLSession = true;
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
            printImportant("error: " + host + ":" + port);
            print("error: " + host+":"+port);
            try {
                source.close();
            } catch (Exception ex) {

            }
        }
    }

    private String[] readPlainInputFromSource() throws IOException, InterruptedException {
        ArrayList<String> inputs = new ArrayList<String>();
        print("start read from Source");
        while (!sourceInput.ready()) {
            Thread.sleep(1000);
            print("sleep: 1000");
        }
        while (true) {
            String s = sourceInput.readLine();
            if (s == null || s.isEmpty()) {
                print("source end");
                break;
            }
            inputs.add(s);
            print(s);
        }
        print("source read end");

        String[] result = inputs.toArray(new String[inputs.size()]);
        return result;
    }

    private void print(Object obj) {
        System.out.print(obj);
        System.out.flush();
    }

    private void printImportant(Object obj) {
        System.out.print(obj);
        System.out.flush();
    }

}
