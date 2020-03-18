package com.jimsshom.androidhttpsniffer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TunnelTask implements Runnable {
    private String name;
    private Socket source;
    private Socket dest;

    public TunnelTask(String name, Socket source, Socket dest) {
        this.name = name;
        this.source = source;
        this.dest = dest;
    }

    @Override
    public void run() {
        try {
            InputStream reader = source.getInputStream();
            OutputStream writer = dest.getOutputStream();
            while (true) {
                int cnt = reader.available();
                //StringBuffer sb = new StringBuffer();
                while (cnt > 0) {
                    byte[] block = new byte[cnt];
                    int blockLen = reader.read(block);
                    if (blockLen == -1) {
                        writer.flush();
                        print(name + ": quit");
                        source.close();
                        dest.close();
                        return;
                    }
                    writer.write(block, 0, blockLen);
                    //sb.append(ch);
                }
                if (cnt > 0) {
                    writer.flush();
                }
                //if (sb.length() > 0) {
                //print(name + ": " + sb.toString());
                //}
                Thread.sleep(5);
            }
        } catch (Exception e) {
            try {
                printImportant("error: " + name);
                source.close();
                dest.close();
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    private void printImportant(Object obj) {
        System.out.print(obj);
        System.out.flush();
    }

    private void print(Object obj) {
        //System.out.print(obj);
        //System.out.flush();
    }

}
