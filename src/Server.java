import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class Server {
	public static final int PORT = 12345;
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
			this.showConnectedClients(clientSocket);
			clients.add(clientSocket);
			new Thread(() -> this.clientMessageLoop(clientSocket)).start(); // lambda expression
		}
	}
	
	private void clientMessageLoop(ClientSocket clientSocket) { // loop enquanto o cliente está no chat; recebe as mensagens dos clientes, imprime no cmd e envia aos outros clientes
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
	
	private void sendMessageToAll(ClientSocket messenger, String msg, boolean serverMessage) { // "broadcast", manda para todos os clientes
		Iterator<ClientSocket> iterator = clients.iterator();
		if(serverMessage) // se for uma msg do servidor para todos (cliente entrou/saiu), diz que é do servidor
			msg = "[Servidor] " + messenger.getRemoteSocketAddress() + msg;
		else
			msg = messenger.getRemoteSocketAddress() + " > " + msg;
		
		while(iterator.hasNext()) {
			ClientSocket clientSocket = iterator.next();
			if(!clientSocket.equals(messenger)) { // não deixa mandar a msg para quem a enviou
				if(!clientSocket.sendMessage(msg)) {
					iterator.remove();
				}
			}
		}
	}
	
	private void showConnectedClients(ClientSocket requester) { // mostra os clientes conectados
		String msg = "[Servidor] ";
		Iterator<ClientSocket> iterator = clients.iterator();
		
		if(!iterator.hasNext()) {
			msg += "Nenhum usuario online.";
		} else {
			msg += "Clientes conectados: ";
			while(iterator.hasNext()) {
				msg += iterator.next().getRemoteSocketAddress();
				if(iterator.hasNext())
					msg += ", ";
				else
					msg += ".";
			}
			
			requester.sendMessage(msg);
		}
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.start();
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Servidor finalizado.");
		}
	}
}