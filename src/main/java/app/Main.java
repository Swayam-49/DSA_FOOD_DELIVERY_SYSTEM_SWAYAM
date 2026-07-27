package app;

import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

/**
 * Entry point of the QuickBites food delivery application.
 * Starts the local Java HttpServer and opens the browser.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("      QuickBites - Food Delivery System      ");
        System.out.println("=============================================");
        System.out.println("Initializing In-Memory Database...");
        
        // Force initialization of DataStore static blocks
        try {
            Class.forName("app.DataStore");
            System.out.println("DataStore pre-populated successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load DataStore: " + e.getMessage());
            return;
        }

        System.out.println("Starting Java Built-in HTTP Server...");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Route all traffic to our Router handler
            server.createContext("/", new Router());
            
            // Use a cached thread pool to handle concurrent browser requests smoothly
            server.setExecutor(Executors.newCachedThreadPool());
            
            server.start();
            
            String localUrl = "http://localhost:" + PORT;
            System.out.println("Server successfully started and listening on: " + localUrl);
            System.out.println("Attempting to open your browser...");
            
            openBrowser(localUrl);

            System.out.println("---------------------------------------------");
            System.out.println("Press Ctrl+C in this console to stop server.");
            System.out.println("=============================================");
            
        } catch (IOException e) {
            System.err.println("Fatal: Failed to start HTTP server on port " + PORT);
            System.err.println("Error details: " + e.getMessage());
            System.err.println("Please check if another process is using port " + PORT + ".");
        }
    }

    /**
     * Helper to open the default web browser on Windows, macOS, or Linux.
     */
    private static void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows command execution is fast and robust
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                // macOS command
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux command
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            } else {
                // AWT Desktop fallback
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    System.out.println("Browser automation not supported on this platform.");
                    System.out.println("Please open your browser manually and visit: " + url);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not auto-start browser: " + e.getMessage());
            System.out.println("Please open your browser manually and navigate to: " + url);
        }
    }
}
