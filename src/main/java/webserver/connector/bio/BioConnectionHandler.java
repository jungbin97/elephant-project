package webserver.connector.bio;

import webserver.connector.RequestHandler;
import webserver.container.StandardContext;

import java.net.Socket;

public class BioConnectionHandler {
    private final StandardContext standardContext;

    public BioConnectionHandler(StandardContext context) {
        this.standardContext = context;
    }

    public void process(Socket socket) {
        new RequestHandler(socket, standardContext).run();
    }
}
