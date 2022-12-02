import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements Runnable {
	private static final String SERVER_ADDRESS = "127.0.0.1";
	private ClientSocket clientSocket;
	private Scanner scan;
	static final String EXIT_COMMAND = "QUIT";
	
	public Client() {
		scan = new Scanner(System.in);
	}
	
	public void start() throws UnknownHostException, IOException {
		try {
			clientSocket = new ClientSocket(new Socket(SERVER_ADDRESS, Server.PORT));
			System.out.println(SERVER_ADDRESS + "\n");
			System.out.println("Entrou no chat porta " + Server.PORT + ", identificado como cliente porta " + clientSocket.getLocalPort() + ".");
			System.out.println("Digite uma mensagem ou digite \"" + EXIT_COMMAND + "\" para sair.\n");

			new Thread(this).start(); // executa o run
			
			this.messageLoop();
		} finally {
			if(clientSocket != null) {
				clientSocket.close();
				System.out.println("Você saiu do chat.");
			}
		}
	}
	
	private void messageLoop() throws IOException { // loop; lê as msgs digitadas e envia ao servidor
		String str;
		do {
			str = scan.nextLine();
			if(!str.isBlank())
				clientSocket.sendMessage(str);
		} while(!str.equals(EXIT_COMMAND));
	}
	
	@Override
	public void run() { // recebe as mensagens (em uma thread separada)
		String str;
		while((str = clientSocket.getMessage()) != null) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) {
		try {
			Client client = new Client();
			client.start();
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
}