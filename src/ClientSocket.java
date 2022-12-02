import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket {
	private final Socket socket;
	private final BufferedReader in;
	private final PrintWriter out;
	
	public ClientSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	public void close() {
		try {
			this.socket.close();
			this.in.close();
			this.out.close();
		} catch(IOException e) {
			System.out.println("Erro ao fechar socket: " + e.getMessage());
		}
	}
	
	public String getMessage() {
		try {
			return this.in.readLine();
		} catch(IOException e) {
			return null;
		}
	}

	public boolean sendMessage(String msg) {
		this.out.println(msg);
		return !out.checkError();
	}
	
	public SocketAddress getRemoteSocketAddress() {
		return this.socket.getRemoteSocketAddress();
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
}
