import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client extends JFrame{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	private int height = 600;
	private int width = 600;
	private DefaultListModel<String> myModel = new DefaultListModel<>();
	private DefaultListModel<String> myModel2 = new DefaultListModel<>();
	private String filNameServer = "";
	private String filNameClient = "";
	private Path serverPath = Paths.get("server");
	private Path clientPath = Paths.get("c:\\");
	private FileCloudHandler cloudHandler = new FileCloudHandler(serverPath,clientPath,in,out, socket);
	private ListHandler listHandler = new ListHandler(serverPath, socket);


	public Path getServerPath() {
		return serverPath;
	}

	public void setServerPath(Path serverPath) {
		this.serverPath = serverPath;
	}

	public Client() throws IOException {
		socket = new Socket("localhost", 1234);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		cloudHandler.setSocket(socket);
		cloudHandler.setIn(in);
		cloudHandler.setOut(out);
		listHandler.setSocket(socket);
		listHandler.setIn(in);
		listHandler.setOut(out);

		auth();
	}


	public void auth() {
		JFrame frameAuth = new JFrame("authorization");
		frameAuth.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameAuth.setSize(new Dimension(400,110));
		frameAuth.setLocationRelativeTo(null);
		JSplitPane panelAuth = new JSplitPane();
		JSplitPane panelAuthLabel = new JSplitPane();
		Label login = new Label("login");
		Label pass = new Label("password");
		login.setMaximumSize(new Dimension(150,10));
		pass.setMaximumSize(new Dimension(150,10));
		JButton buttonAuth = new JButton("authorization");
		buttonAuth.setSize(20,40);
		JButton buttonReg = new JButton("registration");
		buttonReg.setSize(20,40);
//		JButton changePassButton = new JButton("change pass");
//		changePassButton.setSize(20,40);
		JPasswordField passwordField = new JPasswordField();
		JTextArea taAuth = new JTextArea();
		JPanel panelButton = new JPanel();
		panelButton.add(buttonAuth);
		panelButton.add(buttonReg);
//		panelButton.add(changePassButton);
		panelAuth.setLeftComponent(taAuth);
		panelAuth.setRightComponent(passwordField);
		panelAuth.setResizeWeight(0.5);
		panelAuthLabel.setLeftComponent(login);
		panelAuthLabel.setRightComponent(pass);
		panelAuthLabel.setResizeWeight(0.55);
		frameAuth.getContentPane().add(BorderLayout.NORTH, panelAuth);
		frameAuth.getContentPane().add(BorderLayout.CENTER, panelAuthLabel);
		frameAuth.getContentPane().add(BorderLayout.SOUTH, panelButton);

		buttonAuth.addActionListener(a->{
			try {
				if (authorizatior(taAuth,passwordField)==1){
					System.out.println("if buttonAuth");
						runClient();
						frameAuth.setVisible(false);
				}
				else {
					taAuth.setText("Wrong login or password");
					passwordField.setText("");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}


		});

		buttonReg.addActionListener(a->{
			try {
				if (registr(taAuth,passwordField)==1){
					System.out.println("if buttonReg");
					runClient();
					frameAuth.setVisible(false);
				}
				else {
					taAuth.setText("this login already used");
					passwordField.setText("");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		});

//		changePassButton.addActionListener(a->{
//			frameAuth.setVisible(false);
//			changePass();
//		});



		frameAuth.setVisible(true);

	}
//	public void changePass(){
//		JFrame frameChangePass = new JFrame("changePass");
//		frameChangePass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frameChangePass.setSize(new Dimension(400,110));
//		frameChangePass.setLocationRelativeTo(null);
//		JSplitPane panelAuth = new JSplitPane();
//		JSplitPane panelAuthLabel = new JSplitPane();
//		JSplitPane paneChange = new JSplitPane();
//		Label login = new Label("login");
//		Label pass = new Label("Old password");
//		Label newPass = new Label("new password");
//		login.setMaximumSize(new Dimension(150,10));
//		pass.setMaximumSize(new Dimension(150,10));
//
//		JButton changePassButton = new JButton("change pass");
//		changePassButton.setSize(20,40);
//		JPasswordField passwordField = new JPasswordField();
//		JTextArea taAuth = new JTextArea();
//		JPanel panelButton = new JPanel();
//
//		panelButton.add(changePassButton);
//		panelAuth.setLeftComponent(taAuth);
//		panelAuth.setRightComponent(passwordField);
//		panelAuth.setResizeWeight(0.5);
//		panelAuthLabel.setLeftComponent(login);
//		panelAuthLabel.setRightComponent(pass);
//		panelAuthLabel.setResizeWeight(0.55);
//		paneChange.setBottomComponent(newPass);
//		frameChangePass.getContentPane().add(BorderLayout.NORTH, panelAuth);
//		frameChangePass.getContentPane().add(BorderLayout.CENTER, panelAuthLabel);
//		frameChangePass.getContentPane().add(BorderLayout.SOUTH, panelButton);
//		frameChangePass.getContentPane().add(paneChange);
//
//		frameChangePass.setVisible(true);
//
//
//	}

	public int authorizatior(JTextArea taAuth, JPasswordField passwordField) throws IOException {
		String msg = "auth\n" + taAuth.getText() + "\n" + passwordField.getText() + "\n";
		out.write(msg.getBytes(StandardCharsets.UTF_8));
		System.out.println("waiting in");
		int b = Integer.parseInt(cloudHandler.readMsg(in));
		System.out.println(b);
		return b;
	}

	public int registr (JTextArea taAuth, JPasswordField passwordField) throws IOException {
		String msg = "reg\n" + taAuth.getText()+"\n" + passwordField.getText()+"\n";
		out.write(msg.getBytes(StandardCharsets.UTF_8));
		System.out.println("waiting in");
		int b = Integer.parseInt(cloudHandler.readMsg(in));
		System.out.println(b);
		return b;
	}

	private void runClient() throws IOException {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel();

		JSplitPane panel2 = new JSplitPane();
		JSplitPane panalTa = new JSplitPane();
		JTextArea taS = new JTextArea();
		JTextArea taC = new JTextArea();
		taS.setSize(150,10);
		taC.setSize(150,10);
		Icon iconRefresh = new ImageIcon("ClientApp/src/main/resources/icon/refresh2.png");



		JList<String> list = new JList<>();
		JList<String> list2 = new JList<>();
		list.setModel(myModel);
		list2.setModel(myModel2);
		JButton uploadButton = new JButton("Upload");
		uploadButton.setSize(20,40);

		JButton downloadButton = new JButton("download");
		downloadButton.setSize(20,40);

		JButton removeButton = new JButton("remove");
		removeButton.setSize(20,40);

		JButton refreshButton = new JButton(iconRefresh);
		refreshButton.setSize(20,20);

		JButton createFolder = new JButton("createFolder");
		createFolder.setSize(20,40);

		JButton changePassButton = new JButton("change pass");
		changePassButton.setSize(20,40);


		frame.getContentPane().add(BorderLayout.NORTH, panalTa);
		frame.getContentPane().add(BorderLayout.CENTER, panel2);
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);
		panel.add(refreshButton);
		panel.add(createFolder);
		panel.add(changePassButton);

		panel2.setLeftComponent(new JScrollPane(list));
		panel2.setRightComponent(new JScrollPane(list2));
		panel2.setResizeWeight(0.5);
		panalTa.setLeftComponent(taS);
		panalTa.setRightComponent(taC);
		panalTa.setResizeWeight(0.54);
		panalTa.setSize(99,10);

		serverPath = cloudHandler.getServerPath();
		clientPath = cloudHandler.getClientPath();
		listHandler.setServerPath(serverPath);
		listHandler.setClientPath(clientPath);

		listHandler.fillList(myModel, serverPath);
		listHandler.clientList(myModel2, clientPath, "out");



		frame.setVisible(true);


		createFolder.addActionListener(a-> {
			cloudHandler.setClientPath(clientPath);
			cloudHandler.setServerPath(serverPath);
			if ((taC.getText().equals(""))&&(taS.getText().equals(""))) {
				System.out.println("null name folder");
			} else if (taC.getText().equals("")) {
				try {
					cloudHandler.createFolder(taS, "left");
					listHandler.fillList(myModel, serverPath);
					taS.setText("");
				} catch (IOException e) {
					e.printStackTrace();
				}


			} else if (taS.getText().equals("")) {
				System.out.println(clientPath);
				if(listHandler.getClientPath().toString().length()>4) {
					try {
						cloudHandler.createFolder(taC, "right");
						listHandler.fillList(myModel2, clientPath);
						taC.setText("");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				listHandler.clientList(myModel2, clientPath);
			}
		});


		uploadButton.addActionListener(a -> {
			cloudHandler.setClientPath(clientPath);
			cloudHandler.sendFile(taC.getText());
			try	{
				listHandler.fillList(myModel, serverPath);
				listHandler.clientList(myModel2, clientPath);
			} catch (IOException e){
				e.printStackTrace();
			}

		});
		downloadButton.addActionListener(a -> {
			cloudHandler.setServerPath(serverPath);
			cloudHandler.setClientPath(clientPath);
			cloudHandler.downloadFile(taS.getText());
			try	{
				listHandler.fillList(myModel, serverPath);
				listHandler.clientList(myModel2, clientPath);
			} catch (IOException e){
				e.printStackTrace();
			}
		});
		removeButton.addActionListener(a -> {
			if(taC.getText().equals("")) {
				cloudHandler.setServerPath(serverPath);
				cloudHandler.remove(taS.getText(), "server");
				try {
					listHandler.fillList(myModel, serverPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				cloudHandler.setClientPath(clientPath);
				cloudHandler.remove(taC.getText(), "client");
				listHandler.clientList(myModel2, clientPath);
			}
		});
		refreshButton.addActionListener(a -> {
			try {
				listHandler.fillList(myModel, serverPath);
				listHandler.clientList(myModel2, clientPath);;
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		list.addListSelectionListener(a ->{
				taC.setText("");
				taS.setText(list.getSelectedValue());

		});
		list2.addListSelectionListener(a ->{
			taS.setText("");
			taC.setText(list2.getSelectedValue());
		});

		//addListSelectionListener to the server window
		list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						if (taS.getText().equals("...")){
							String[] p = serverPath.toString().split("\\\\");
							if (p.length > 1) {
								String pah = "";
								for (int i = 0; i < p.length - 1; i++) {
									pah += p[i] + "\\";
								}
								serverPath = Paths.get(pah);
							}
						} else {
							serverPath = Paths.get(serverPath + File.separator + taS.getText());
						}
						try {
							listHandler.fillList(myModel, serverPath);
						} catch (IOException ioException) {
							ioException.printStackTrace();
						}

					}
				}
			});

		//addListSelectionListener to the client window

			list2.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {

					if (e.getClickCount() == 2) {
						if ((clientPath.toString().length()<4)&&(taC.getText().contains(":\\"))){
							clientPath=Path.of(taC.getText());
							listHandler.clientList(myModel2, clientPath);
						}
						if (Files.isDirectory(Paths.get(clientPath.toString() + File.separator + taC.getText()))) {
							if ((taC.getText().equals("...") )){ //|| taS.getText().equals("...")
								if (clientPath.toAbsolutePath().normalize().getParent()!=null) {
									clientPath = clientPath.normalize().toAbsolutePath().getParent();
									listHandler.clientList(myModel2, clientPath);
								} else {
									clientPath=Path.of(taC.getText());
									listHandler.clientList(myModel2, clientPath, "out");
								}
							}else {
								clientPath = Path.of(clientPath + File.separator + taC.getText());
								listHandler.clientList(myModel2, clientPath);
							}
						}
					}

				}

			});
	}






	public static void main(String[] args) throws IOException {
//		new Client();
		CloudGUI cloudGUI = new CloudGUI();
		cloudGUI.setSize(800,800);
		cloudGUI.setLocationRelativeTo(null);
		cloudGUI.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}
}
