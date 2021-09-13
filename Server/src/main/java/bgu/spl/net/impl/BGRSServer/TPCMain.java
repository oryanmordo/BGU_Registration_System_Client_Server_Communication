package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.MessageProtocol;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {


        Server.threadPerClient(
                Integer.parseInt(args[0]), // port
                MessageProtocol::new,
                LineMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();






}}
