package ChatClient;

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ClientMain extends JFrame {
	private Socket socket;

	private ArrayList<String> wordList;

	private boolean myTurn = false;

	private JPanel panel;
	private JTextField textField;
	private JTextField userName;
	private JTextField IPText;
	private JTextField portText;
	private JLabel guidance;
	private JTextField inputText;
	private JButton sendButton;
	private JButton connectionButton;
	private JTextArea definition;

	public ClientMain() {
		super("끝말잇기");

		wordList = new ArrayList<String>();

		panel = new JPanel();
		panel.setLayout(null);

		userName = new JTextField("홍길동");
		userName.setBounds(15, 5, 135, 25);
		panel.add(userName);
		IPText = new JTextField("127.0.0.1"); // 10.96.123.53
		IPText.setBounds(155, 5, 135, 25);
		panel.add(IPText);
		portText = new JTextField("9876");
		portText.setBounds(295, 5, 135, 25);
		panel.add(portText);

		connectionButton = new JButton("접속하기");
		connectionButton.setBounds(440, 5, 90, 23);
		connectionButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 15));
		connectionButton.requestFocus();
		connectionButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (connectionButton.getText().equals("접속하기")) {
					int port = 9876;
					try {
						port = Integer.parseInt(portText.getText());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					startClient(IPText.getText(), port);
					connectionButton.setText("종료하기");
				} else {
					myTurn = false;
					stopClient();
					connectionButton.setText("접속하기");
					inputText.setEditable(false);
					sendButton.setVisible(false);
				}
			}
		});
		panel.add(connectionButton);

		guidance = new JLabel();
		guidance.setBounds(75, 35, 400, 20);
		guidance.setHorizontalAlignment(JLabel.CENTER);
		guidance.setFont(new Font("나눔바른고딕", Font.PLAIN, 15));
		guidance.setText("");
		panel.add(guidance);

		textField = new JTextField();
		textField.setBounds(50, 60, 450, 40);
		textField.setFont(new Font("나눔바른고딕", Font.PLAIN, 25));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		textField.setText("");
		panel.add(textField);

		inputText = new JTextField();
		inputText.setBounds(50, 115, 365, 30);
		inputText.setFont(new Font("나눔바른고딕", Font.PLAIN, 20));
		inputText.setHorizontalAlignment(JTextField.CENTER);
		inputText.setText("");
		inputText.setEditable(false);
		inputText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					sendButton.doClick();
				}
			}
		});
		panel.add(inputText);

		sendButton = new JButton("전송");
		sendButton.setBounds(550 - 50 - 80, 115, 80, 30);
		sendButton.setFont(new Font("나눔바른고딕", Font.PLAIN, 15));
		sendButton.setVisible(false);
		sendButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				String inputStr = inputText.getText().trim();
				boolean inspectionWord = inspectionWord(inputStr);
				boolean checkLength = inputStr.length() >= 2 ? true : false;
				boolean checkOverlap = checkOverlap(inputStr);
				boolean existDict = existenceDictionary(inputStr);

				if (inspectionWord && checkLength && checkOverlap && existDict) {
					wordList.add(inputStr);
					guidance.setText("");
					send(inputStr);
				} else if (!inspectionWord) {
					guidance.setText("마지막 글자와 첫 글자가 맞지 않습니다.");
				} else if (!checkLength) {
					guidance.setText("글자 수가 너무 적습니다.");
				} else if (!checkOverlap) {
					guidance.setText("이전에 나왔던 단어입니다.");
				} else if (!existDict) {
					guidance.setText("사전에 없는 단어입니다.");
				}
			}
		});
		panel.add(sendButton);

		definition = new JTextArea();
		definition.setBounds(50, 150, 450, 75);
		definition.setFont(new Font("나눔바른고딕", Font.PLAIN, 20));
		definition.setLineWrap(true);
		definition.setEditable(false);
		definition.setText("");
		panel.add(definition);
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

	// 클라이언트 프로그램 동작
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);

					// 첫순서면 채팅 활성화
					InputStream in = socket.getInputStream();
					byte[] buffer = new byte[512];
					int length = in.read(buffer);
					if (length == -1) {
						throw new IOException();
					}
					String message = new String(buffer, 0, length, "UTF-8");
					textField.setText("");
					if (message.trim().equals("1"))
						myTurn = true;
					if (myTurn) {
						inputText.setEditable(true);
						sendButton.setVisible(true);
						myTurn = false;
					} else {
						inputText.setText("상대방이 입력 중입니다.");
						inputText.setEditable(false);
						sendButton.setVisible(false);
						myTurn = true;
					}

					receive();
				} catch (Exception e) {
					if (!socket.isClosed()) {
						stopClient();
						System.out.println("[]");
						System.exit(0);
					}
				}
			}
		};
		thread.start();
	}

	// 클라이언트 프로그램 종료
	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 메시지 수신
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

				definition.setText(getDefinition(message));

				if (myTurn) {
					inputText.setEditable(true);
					sendButton.setVisible(true);
					myTurn = false;
				} else {
					inputText.setText("상대방이 입력 중입니다.");
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

	// 메시지 전송
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

	// 첫글자와 끝글자가 맞는지 확인
	public boolean inspectionWord(String input) {
		char last = ' ';		// 상대방이 입력한 단어의 마지막 글자
		char first;				// 내가 입력할 단어의 첫번째 글자
		try {
			last = input.charAt(0);
			first = textField.getText().charAt(textField.getText().length() - 1);
		} catch (StringIndexOutOfBoundsException e2) {
			first = last;
		}

		if (last == first) {
			return true;
		} else {
			if (isInitialLaw(first, last)) {
				return true;
			}
			return false;
		}
	}

	public boolean isInitialLaw(char first, char last) { // 두음법칙
		String[] CHO = { "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ",
				"ㅎ" };
		String[] JOONG = { "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ",
				"ㅡ", "ㅢ", "ㅣ" };
		String[] JONG = { "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ",
				"ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ" };

		if (first >= 0xAC00 && last >= 0xAC00) { // 한글일 경우만 시작해야 하기 때문에 0xAC00부터 아래의 로직을 실행한다
			// System.out.print(first + "=>");
			first = (char) (first - 0xAC00);
			char first_cho = (char) (first / 28 / 21);
			char first_joong = (char) ((first) / 28 % 21);
			char first_jong = (char) (first % 28); // 종성의 첫번째는 채움이기 때문에
			// System.out.println(CHO[first_cho] + JOONG[first_joong] + JONG[first_jong]);

			// System.out.print(last + "=>");
			last = (char) (last - 0xAC00);
			char last_cho = (char) (last / 28 / 21);
			char last_joong = (char) ((last) / 28 % 21);
			char last_jong = (char) (last % 28); // 종성의 첫번째는 채움이기 때문에
			// System.out.println(CHO[last_cho] + JOONG[last_joong] + JONG[last_jong]);

			if (CHO[first_cho].equals("ㄴ") && CHO[last_cho].equals("ㅇ")) {
				// System.out.println("ㄴ  -> ㅇ");
				if (JOONG[first_joong].equals(JOONG[last_joong]) && JONG[first_jong].equals(JONG[last_jong])) {
					return true;
				}
			} else if (CHO[first_cho].equals("ㄹ") && (CHO[last_cho].equals("ㅇ") || CHO[last_cho].equals("ㄴ"))) {
				// System.out.println("ㄹ -> ㅇ, ㄴ");
				if (JOONG[first_joong].equals(JOONG[last_joong]) && JONG[first_jong].equals(JONG[last_jong])) {
					return true;
				}
			}
		}

		return false;
	}

	// 단어 중복 체크
	public boolean checkOverlap(String input) {
		for (String word : wordList) {
			if (word.equals(input)) {
				return false;
			}
		}
		return true;
	}

	// 단어가 사전에 있는 지 확인
	public boolean existenceDictionary(String input) {
		try {
			// parsing할 url 지정(API 키 포함해서)
			String url = "https://stdict.korean.go.kr/api/search.do?certkey_no=2403&key=AA0EB289572D8E8198B28D56E7DA1BF4&type_search=search&q="
					+ input;

			DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
			Document doc = dBuilder.parse(url);

			// 파싱할 tag
			NodeList nList = doc.getElementsByTagName("item");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
//					System.out.println("----------------------------------");
//					System.out.println("target_code : " + getTagValue("target_code", eElement));
//					System.out.println("word : " + getTagValue("word", eElement));
//					System.out.println("pos : " + getTagValue("pos", eElement));
//					System.out.println("definition : " + getTagValue("definition", eElement1));
					if (getTagValue("word", eElement).length() > 0 && getTagValue("pos", eElement).equals("명사")) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getTagValue(String tag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		if (nValue == null)
			return null;
		return nValue.getNodeValue();
	}

	public String getDefinition(String input) { // 단어의 정의
		try {
			String url = "https://stdict.korean.go.kr/api/search.do?certkey_no=2403&key=AA0EB289572D8E8198B28D56E7DA1BF4&type_search=search&q="
					+ input;

			DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
			Document doc = dBuilder.parse(url);

			NodeList nList = doc.getElementsByTagName("sense");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					return getTagValue("definition", eElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		new ClientMain();
	}
}
