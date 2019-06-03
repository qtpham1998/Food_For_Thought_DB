import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {
    
    public WebServer() throws Exception {
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new Website()), "/*");
        server.setHandler(handler);
        
        server.start();
    }
    
    public static void main(String[] args) throws Exception{
        new WebServer();
    }
}