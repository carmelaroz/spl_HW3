package bgu.spl.net.impl.BGSServer;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.BidiMessagingProtocolImpl;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Reactor;

import java.util.function.Supplier;

public class ReactorMain {
    public static void main(String[] args) {
        Supplier<BidiMessagingProtocol> b = () -> new BidiMessagingProtocolImpl();
        Supplier<MessageEncoderDecoder> m = () -> new MessageEncoderDecoderImpl();

        try (Reactor server = BaseServer.reactor(3,8888, b, m)) {
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
