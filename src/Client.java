import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements Runnable {
	private static final String SERVER_ADDRESS = "127.0.0.1";
	private ClientSocket clientSocket;
	private Scanner scanner;
	static final String EXIT_COMMAND = "/exit";
	
	public Client() {
		scanner = new Scanner(System.in);
	}
	
	public void start() throws UnknownHostException, IOException {
		try {
			clientSocket = new ClientSocket(new Socket(SERVER_ADDRESS, Server.PORT));
			System.out.println(SERVER_ADDRESS + "\n");
			System.out.println("Entrou no chat porta " + Server.PORT + ", identificado como cliente porta " + clientSocket.getLocalPort() + ".");
			System.out.println("Digite uma mensagem ou digite \"" + EXIT_COMMAND + "\" para sair.\n");

			new Thread(this).start();
			
			this.messageLoop();
		} finally {
			if(clientSocket != null) {
				clientSocket.close();
				System.out.println("Você saiu do chat.");
			}
		}
	}
	
	private void messageLoop() throws IOException {
		String str;
		do {
			str = scanner.nextLine();
			if(!str.isBlank())
				clientSocket.sendMessage(str);
		} while(!str.equals(EXIT_COMMAND));
	}
	
	@Override
	public void run() { // recebe as mensagens (em uma thread separada)
		String msg;
		while((msg = clientSocket.getMessage()) != null) {
			System.out.println(msg);
		}
	}
	
	public static void main(String[] args) {
		try {
			Client client = new Client();
			client.start();
		} catch(IOException e) {
			System.out.println("Erro ao iniciar cliente: " + e.getMessage());
		}
	}
}