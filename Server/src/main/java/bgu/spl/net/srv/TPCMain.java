package bgu.spl.net.srv;

import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;

public class TPCMain {
    public static void main(String[] args) {


        Server.threadPerClient(
                Integer.parseInt(args[0]), // port
                MessageProtocol::new,
                LineMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();






}}
