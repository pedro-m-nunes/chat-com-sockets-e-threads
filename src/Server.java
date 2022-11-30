import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * https://www.youtube.com/watch?v=MtAfYUW7fJ4
 * https://www.youtube.com/watch?v=KPASiXiD9zQ
 * https://www.devmedia.com.br/java-sockets-criando-comunicacoes-em-java/9465
 */

public class Server {
	public static final int PORT = 12345; // 4000
	private ServerSocket serverSocket;
	private final List<ClientSocket> clients = new LinkedList<>();
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Servidor iniciado na porta " + serverSocket.getLocalPort() + ".");
		
		this.clientConnectionLoop();
	}
	
	private void clientConnectionLoop() throws IOException {
		while(true) {
			ClientSocket clientSocket = new ClientSocket(serverSocket.accept());
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " entrou no chat.");
			this.sendMessageToAll(clientSocket, " entrou no chat.", true);
			clients.add(clientSocket);
			new Thread(() -> this.clientMessageLoop(clientSocket)).start(); // lambda expression
		}
	}
	
	private void clientMessageLoop(ClientSocket clientSocket) {
		String msg;
		try {
			while((msg = clientSocket.getMessage()) != null) {
				if(msg.equals(Client.EXIT_COMMAND)) {
					return;
				}
				
				System.out.println(clientSocket.getRemoteSocketAddress() + " > " + msg);
				this.sendMessageToAll(clientSocket, msg, false);
			}
		} finally {
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " saiu no chat.");
			this.sendMessageToAll(clientSocket, " saiu do chat.", true);
			clientSocket.close();
		}
	}
	
	private void sendMessageToAll(ClientSocket sender, String msg, boolean serverMessage) { // broadcast, manda para todos os clientes
		Iterator<ClientSocket> iterator = clients.iterator();
		if(serverMessage)
			msg = "[Servidor] " + sender.getRemoteSocketAddress() + msg;
		else
			msg = sender.getRemoteSocketAddress() + " > " + msg;
		
		while(iterator.hasNext()) {
			ClientSocket clientSocket = iterator.next();
			if(!clientSocket.equals(sender)) { // não deixa mandar a msg para quem a enviou
				if(!clientSocket.sendMessage(msg)) {
					iterator.remove();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.start();
		} catch(IOException e) {
			System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
		}
		
		System.out.println("Servidor finalizado.");
	}
}