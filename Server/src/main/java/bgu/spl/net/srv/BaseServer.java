package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.ConnectionHandlerImpl;
import bgu.spl.net.api.ConnectionsImpl;
import bgu.spl.net.api.MessageEncoderDecoder;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private int connectionId = 0;
    private ConnectionsImpl connections = ConnectionsImpl.getInstance();
    private ServerSocket sock;

    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        this.connections.getFilteredWords().add("Banana");
        this.connections.getFilteredWords().add("Gerev");
        this.connections.getFilteredWords().add("COVID");
        this.connections.getFilteredWords().add("sababa");
        this.connections.getFilteredWords().add("bahamas");

//        this.connections = ConnectionsImpl.getInstance();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                ConnectionHandlerImpl<T> handler = new ConnectionHandlerImpl<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());
                this.connections.getConnectionsMap().put(connectionId,handler);
                handler.sign(this.connectionId);
                connectionId++;
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(ConnectionHandlerImpl<T>  handler);


    public static <T> BaseServer<T> threadPerClient(int port,Supplier<BidiMessagingProtocol<T>> protocolFactory,Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory) {
            @Override
            protected void execute(ConnectionHandlerImpl<T> handler) {
                new Thread(handler).start();
            }
        };
    }
    public static Reactor reactor(int nThreads, int port, Supplier<BidiMessagingProtocol> protocolFactory, Supplier<MessageEncoderDecoder> encoderDecoderFactory) {
        return new Reactor(nThreads,port, protocolFactory, encoderDecoderFactory) {
        };
}}
