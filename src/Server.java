import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator;
import java.util.LinkedList;

//histórico de msgs (?), clientes saem quando o servidor cai, nome de usuário, comandos '/' (ver usuarios online...)

public class Server implements Runnable {
	static final int PORT = 12345;
	static final String EXIT_COMMAND = "QUIT";
	private final String SERVER_MSG_PREFIX = "[Servidor]";
	
	private ServerSocket serverSocket;
	private final List<ClientSocket> clients = new LinkedList<>();
	private Scanner scan;
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Servidor iniciado na porta " + serverSocket.getLocalPort() + ".\nDigite uma mensagem ou digite \"" + EXIT_COMMAND + "\" para fechar o servidor.\n");
		
		scan = new Scanner(System.in);
		new Thread(this).start();
		
		this.clientConnectionLoop();
	}
	
	private void clientConnectionLoop() throws IOException { // conexão dos clientes
		while(true) {
			ClientSocket clientSocket = new ClientSocket(serverSocket.accept());
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " entrou no chat.");
			this.sendServerMessage(clientSocket, " entrou no chat.");
			this.showConnectedClients(clientSocket);
			clients.add(clientSocket);
			new Thread(() -> this.clientMessageLoop(clientSocket)).start(); // lambda expression
		}
	}
	
	private void clientMessageLoop(ClientSocket clientSocket) { // loop enquanto o cliente está no chat; recebe as mensagens dos clientes, imprime no cmd e envia aos outros clientes
		String msg;
		try {
			while((msg = clientSocket.getMessage()) != null) {
				if(msg.equals(EXIT_COMMAND)) {
					return;
				}
				
				System.out.println(clientSocket.getRemoteSocketAddress() + " > " + msg);
				this.sendMessageToAll(clientSocket, msg, false);
			}
		} finally {
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " saiu no chat.");
			this.sendServerMessage(clientSocket, " saiu do chat.");
			clientSocket.close();
		}
	}
	
	private void sendMessageToAll(ClientSocket clientInvolved, String msg, boolean serverMessage) { // "broadcast", manda para todos os clientes
		Iterator<ClientSocket> iterator = clients.iterator();
		
		if(!serverMessage) msg = "<" + clientInvolved.getRemoteSocketAddress() + "> " + msg;
		
		while(iterator.hasNext()) {
			ClientSocket clientSocket = iterator.next();
			if(clientInvolved == null || !clientSocket.equals(clientInvolved)) { // não deixa mandar a msg para quem a enviou
				if(!clientSocket.sendMessage(msg)) {
					iterator.remove();
				}
			}
		}
	}
	
	private void sendServerMessage(String msg) {
		msg = SERVER_MSG_PREFIX + " " + msg;
		this.sendMessageToAll(null, msg, true);
	}
	
	private void sendServerMessage(ClientSocket clientInvolved, String msg) {
		msg = SERVER_MSG_PREFIX + " " + clientInvolved.getRemoteSocketAddress() + msg;
		this.sendMessageToAll(clientInvolved, msg, true);
	}
	
	private void sendMessageViaServer(String msg) {
		msg = "<Servidor> " + msg;
		this.sendMessageToAll(null, msg, true);
	}
	
	private void showConnectedClients(ClientSocket requester) { // mostra os clientes conectados
		String msg = SERVER_MSG_PREFIX + " Boas vindas ao chat. ";
		Iterator<ClientSocket> iterator = clients.iterator();
		
		if(!iterator.hasNext()) {
			msg += "Nenhum usuario online.";
		} else {
			String plural = clients.size() == 1 ? "" : "s";
			msg += String.format("%d usuario%s conectado%s: ", clients.size(), plural, plural);
			while(iterator.hasNext()) {
				msg += iterator.next().getRemoteSocketAddress();
				if(iterator.hasNext())
					msg += ", ";
				else
					msg += ".";
			}
		}
		
		requester.sendMessage(msg);
	}
	
	@Override
	public void run() { // entrada do teclado no servidor (thread separada)
		while(true) {
			String msg = scan.nextLine();
			
			if(msg.equals(EXIT_COMMAND)) {
				this.sendServerMessage("Chat fechado pelo administrador. Digite \"" + EXIT_COMMAND + "\" para sair.");
				System.out.println("Servidor finalizado pelo administrador.");
				System.exit(0);
			}
			
			this.sendMessageViaServer(msg);
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