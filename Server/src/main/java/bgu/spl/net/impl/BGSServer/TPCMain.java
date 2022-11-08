package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.BidiMessagingProtocolImpl;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.srv.BaseServer;

import java.io.IOException;
import java.util.function.Supplier;

public class TPCMain {
        public static void main (String[]args) throws IOException {
            Supplier<BidiMessagingProtocol<String>> b = () -> new BidiMessagingProtocolImpl();
            Supplier<MessageEncoderDecoder<String>> m = () -> new MessageEncoderDecoderImpl();

            try (BaseServer<String> server = BaseServer.threadPerClient(8888, b, m)) {
                server.serve();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
