package ChatClient;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClientMain extends JFrame {
	private Socket socket;

	private boolean myTurn = false;

	private JPanel panel;
	private JTextField textField;
	private JTextField userName;
	private JTextField IPText;
	private JTextField portText;
	private JTextField inputText;
	private JButton sendButton;
	private JButton connectionButton;

	public ClientMain() {
		super("�����ձ�");

		panel = new JPanel();

		userName = new JTextField("ȫ�浿", 10);
		panel.add(userName);
		IPText = new JTextField("127.0.0.1", 10);
		panel.add(IPText);
		portText = new JTextField("9876", 10);
		panel.add(portText);

		connectionButton = new JButton("�����ϱ�");
		connectionButton.setFont(new Font("�����ٸ����", Font.PLAIN, 15));
		connectionButton.requestFocus();
		connectionButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (connectionButton.getText().equals("�����ϱ�")) {					
					int port = 9876;
					try {
						port = Integer.parseInt(portText.getText());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					startClient(IPText.getText(), port);
					connectionButton.setText("�����ϱ�");
				} else {
					myTurn = false;
					stopClient();
					connectionButton.setText("�����ϱ�");
					inputText.setEditable(false);
					sendButton.setVisible(false);
				}
			}
		});
		panel.add(connectionButton);

		textField = new JTextField(19);
		textField.setFont(new Font("�����ٸ����", Font.PLAIN, 25));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		panel.add(textField);

		inputText = new JTextField(18);
		inputText.setFont(new Font("�����ٸ����", Font.PLAIN, 20));
		inputText.setEditable(false);
		panel.add(inputText);

		sendButton = new JButton("����");
		sendButton.setFont(new Font("�����ٸ����", Font.PLAIN, 15));
		sendButton.setVisible(false);
		sendButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				send(inputText.getText());	
			}
		});
		panel.add(sendButton);

		add(panel);

		setSize(550, 400);
		setVisible(true);
		setLocationRelativeTo(null);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stopClient();
				System.exit(0);
			}
		});
	}

	// Ŭ���̾�Ʈ ���α׷� ����
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					
					// ù������ ä�� Ȱ��ȭ
					InputStream in = socket.getInputStream();
					byte[] buffer = new byte[512];
					int length = in.read(buffer);
					if (length == -1) {
						throw new IOException();
					}
					String message = new String(buffer, 0, length, "UTF-8");
					textField.setText("");
					if(message.trim().equals("1"))
						myTurn = true;
					if(myTurn) {
						inputText.setEditable(true);
						sendButton.setVisible(true);
						myTurn = false;
					} else {
						inputText.setText("������ �Է� ���Դϴ�.");
						inputText.setEditable(false);
						sendButton.setVisible(false);
						myTurn = true;
					}
					
					receive();
				} catch (Exception e) {
					if (!socket.isClosed()) {
						stopClient();
						System.out.println("[���� ���� ����]");
						System.exit(0);
					}
				}
			}
		};
		thread.start();
	}

	// Ŭ���̾�Ʈ ���α׷� ����
	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �޽��� ����
	public void receive() {
		while (true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if (length == -1) {
					throw new IOException();
				}
				String message = new String(buffer, 0, length, "UTF-8");
				textField.setText(message);
				inputText.setText("");
				
				if(myTurn) {
					inputText.setEditable(true);
					sendButton.setVisible(true);
					myTurn = false;
				} else {
					inputText.setText("������ �Է� ���Դϴ�.");
					inputText.setEditable(false);
					sendButton.setVisible(false);
					myTurn = true;
				}
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}

	// �޽��� ����
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		new ClientMain();
	}
}
