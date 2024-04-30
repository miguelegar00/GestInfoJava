package com.example.gestinfo;

import java.io.IOException;

public class OpenUrlExample {

    public static void main(String[] args) {
        String url = "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/SolicitudConcursoConMasa?id=";
        openUrlInBrowser(url);
    }

    private static void openUrlInBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                String[] browsers = {"firefox", "chrome", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                StringBuilder cmd = new StringBuilder();
                for (String browser : browsers) {
                    cmd.append(String.format("%s \"%s\" || ", browser, url));
                }
                rt.exec(new String[]{"sh", "-c", cmd.toString()});
            } else {
                // Si no se puede determinar el sistema operativo, simplemente intenta abrir la URL
                rt.exec("xdg-open " + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openUrlWithId(String id) {
        String url = "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/SolicitudConcursoConMasa?id=" + id;
        openUrlInBrowser(url);
    }
}