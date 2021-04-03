package ChatServer;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerMain extends JFrame {
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();

	private ServerSocket serverSocket;

	private String IP = "127.0.0.1";	//10.96.123.53
	private int port = 9876;

	private JPanel panel;
	private JTextArea textArea;
	private JButton toggleButton;

	public ServerMain() {
		super("�����ձ� ����");

		panel = new JPanel();

		textArea = new JTextArea(15, 19);
		textArea.setFont(new Font("�����ٸ����", Font.PLAIN, 25));
		textArea.setEditable(false);
		panel.add(textArea);

		toggleButton = new JButton("�����ϱ�");
		toggleButton.setFont(new Font("�����ٸ����", Font.PLAIN, 20));
		toggleButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { // ���콺�� ��������
				if (toggleButton.getText().equals("�����ϱ�")) {
					startServer(IP, port);
					String message = String.format("���� ����\n", IP, port);
					textArea.append(message);

					toggleButton.setText("�����ϱ�");
				} else {
					stopServer();
					String message = String.format("���� ����\n", IP, port);
					textArea.append(message);

					toggleButton.setText("�����ϱ�");
				}
			}
		});

		panel.add(toggleButton);

		add(panel);
		setSize(450, 550);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void startServer(String IP, int port) { // ���� ����
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}

		// Ŭ���̾�Ʈ�� ������ ������ ��ٸ�
		Runnable thread = new Runnable() {
			int count = 0;
			@Override
			public void run() {
				while (true) {
					count++;
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						//System.out.println("[Ŭ���̾�Ʈ ����] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
						
						// Ŭ���̾�Ʈ���� �޽���
						PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
						writer.println(String.valueOf(clients.size()));
						writer.flush();
					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};

		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}

	public void stopServer() { // ���� ����
		try {
			// �۵����� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// ������Ǯ ����
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ServerMain();
	}

}
