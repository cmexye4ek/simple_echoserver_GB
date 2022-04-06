import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) {
        try {
            new Server().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("localhost", 9000));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");

        while (true) {
            selector.select();
            System.out.println("New selector event");
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("New selector acceptable event");
                    register(selector, serverSocket);
                }
                if (selectionKey.isReadable()) {
                    System.out.println("New selector readable event");
                    echoMessage(selectionKey);
                }
                iterator.remove();
            }
        }
    }

    private void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected");
    }

    private void echoMessage(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        do {
            client.read(byteBuffer);
        } while (!(new String(byteBuffer.array()).contains(System.lineSeparator())));
        String message = new String(byteBuffer.array()).trim();
        System.out.println("New message: " + message);
        byteBuffer.flip();
        message = "ECHO: " + message + "\r\n";
        byteBuffer = byteBuffer.wrap(message.getBytes());
        client.write(byteBuffer);
        System.out.println("echo send");
    }
}
