package bgu.spl.net.srv;

import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;

public class ReactorMain {

    public static void main(String[] args) {
        try{Reactor server = new Reactor(
                Integer.parseInt(args[0]), // number of working threads
                Integer.parseInt(args[1]), // port
                MessageProtocol::new,
                LineMessageEncoderDecoder::new);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
