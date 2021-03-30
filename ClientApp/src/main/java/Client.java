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
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private int height = 600;
	private int width = 600;
	private DefaultListModel<String> myModel = new DefaultListModel<>();
	private DefaultListModel<String> myModel2 = new DefaultListModel<>();
	private String filNameServer = "";
	private String filNameClient = "";
	private Path serverPath = Paths.get("server");
	private Path clientPath = Paths.get("c:\\");


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
		int b = Integer.parseInt(readMsg(in));
		System.out.println(b);
		return b;
	}

	public int registr (JTextArea taAuth, JPasswordField passwordField) throws IOException {
		String msg = "reg\n" + taAuth.getText()+"\n" + passwordField.getText()+"\n";
		out.write(msg.getBytes(StandardCharsets.UTF_8));
		System.out.println("waiting in");
		int b = Integer.parseInt(readMsg(in));
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

		fillList(myModel, serverPath);
		clientList(myModel2, clientPath, "out");



		frame.setVisible(true);


		createFolder.addActionListener(a-> {
			if ((taC.getText().equals(""))&&(taS.getText().equals(""))) {
				System.out.println("null name folder");
			} else if (taC.getText().equals("")) {
				try {
					createFolder(taS, "left");
					fillList(myModel, serverPath);
					taS.setText("");
				} catch (IOException e) {
					e.printStackTrace();
				}


			} else if (taS.getText().equals("")) {
				System.out.println(clientPath);
				if(clientPath.toString().length()>4) {
					try {
						createFolder(taC, "right");
						fillList(myModel2, clientPath);
						taC.setText("");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				clientList(myModel2, clientPath);
			}
		});


		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(taC.getText()));
			try	{
				fillList(myModel, serverPath);
				clientList(myModel2, clientPath);
			} catch (IOException e){
				e.printStackTrace();
			}

		});
		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(taS.getText()));
			try	{
				fillList(myModel, serverPath);
				clientList(myModel2, clientPath);
			} catch (IOException e){
				e.printStackTrace();
			}
		});
		removeButton.addActionListener(a -> {
			if(taC.getText().equals("")) {
				System.out.println(remove(taS.getText(), "server"));
				try {
					fillList(myModel, serverPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(remove(taC.getText(), "client"));
				clientList(myModel2, clientPath);
			}
		});
		refreshButton.addActionListener(a -> {
			try {
				fillList(myModel, serverPath);
				clientList(myModel2, clientPath);;
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
							fillList(myModel, serverPath);
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
							clientList(myModel2, clientPath);
						}
						if (Files.isDirectory(Paths.get(clientPath.toString() + File.separator + taC.getText()))) {
							if ((taC.getText().equals("...") || taS.getText().equals("..."))){
								if (clientPath.toAbsolutePath().normalize().getParent()!=null) {
									clientPath = clientPath.normalize().toAbsolutePath().getParent();
									clientList(myModel2, clientPath);
								} else {
									clientPath=Path.of(taC.getText());
									clientList(myModel2, clientPath, "out");
								}
							}else {
								clientPath = Path.of(clientPath + File.separator + taC.getText());
								clientList(myModel2, clientPath);
							}
						}
					}

				}

			});
	}

	private void createFolder(JTextArea textArea, String pos) throws IOException {
		if (pos.equals("right")) {
			if (!new File(clientPath + File.separator + textArea.getText()).exists()) {
				Files.createDirectory(Paths.get(clientPath + (File.separator + textArea.getText())));
			}
		} else if (pos.equals("left")) {
			String m = "createFolder\n" + textArea.getText();
			out.write(m.getBytes(StandardCharsets.UTF_8));
			out.flush();
			readMsg(in);
		} else System.out.println("wrong null name folder");
	}

	private void clientList(DefaultListModel<String> myModel2, Path clientPath){
		if (clientPath.toString().equals("...")){
			clientList(myModel2,clientPath,"out");

		} else {
			File file = new File(clientPath.toString());
			String[] files = file.list();
			myModel2.clear();
			myModel2.addElement("...");
			if (files != null) {
				for (String fil : files) {
					myModel2.addElement(fil);
				}
			}
		}
	}

	private void clientList(DefaultListModel<String> myModel2, Path clientPath, String out){
		String arg = FileSystems.getDefault().getRootDirectories().toString();
		arg = arg.replace("[", "");
		arg = arg.replace("]", "");
		arg = arg.replace(" ", "");
		String[] files = arg.split(",");
		myModel2.clear();
			for (String fil:files){
				myModel2.addElement(fil);
			}
	}

	private void fillList(DefaultListModel<String> myModel, Path path) throws IOException {
		List<String> list =  downloadFileList();
		myModel.clear();
		myModel.addElement("...");
		for (String filename : list) {
			myModel.addElement(filename);
		}
	}

	private List<String> downloadFileList() throws IOException {
		List<String> list = new ArrayList<String>();

		try {
			StringBuilder sb = new StringBuilder();
			out.write(("list-files\n" + serverPath + "\n").getBytes(StandardCharsets.UTF_8));
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if (sb.toString().endsWith("end")) {
					break;
				}
			}
			if (sb.toString().split("\n")[0].equals("false")) {
				String[] p = serverPath.toString().split("\\\\");
				if (p.length > 1) {
					String pah = "";
					for (int i = 0; i < p.length - 1; i++) {
						pah += p[i] + "\\";
					}

					serverPath = Paths.get(pah);
					sb.delete(0, sb.toString().split("\n")[0].length() +1);
				}
				}
				out.flush();

				String fileString = sb.substring(0, sb.toString().length() - 4);
				list = Arrays.asList(fileString.split("\n"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String sendFile(String filename) {
		try {
			File file = new File(clientPath + File.separator + filename);
			if (file.exists()) {
				String msg = ("upload\n" + filename + "\n");
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream inputStream = new BufferedInputStream(fis, 512);
				long size = file.length();
				out.write(msg.getBytes());
				while ((inputStream.available() > 0)) {
					out.write(inputStream.readAllBytes());
				}
				out.flush();
				return readMsg(in);

			} else {
				return "File is not exists";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String downloadFile(String filename) {
		try {
			File file = new File(clientPath + File.separator + filename);
			if (!file.exists()) {
				file.createNewFile();

				out.write(("download\n" + filename+"\n").getBytes(StandardCharsets.UTF_8));
				out.flush();

				FileOutputStream fos = new FileOutputStream(clientPath + File.separator + filename);
				while (true) {
					byte[] buffer = new byte[512];
					int size = in.read(buffer);
					fos.write(buffer,0,size);
					if (in.available()<1) {
						break;
					}
				}
				fos.close();

				return readMsg(in);
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String remove(String filename, String position){
		try {
			if (position.equals("server")){
			File file = new File(serverPath + File.separator + filename);
			if (file.exists()) {
				String msg = "remove " + filename;
				out.write(msg.getBytes(StandardCharsets.UTF_8));
				out.flush();
				return readMsg(in);

			} else {
				return "File is not exists";
			}
			}
			else {
				File file = new File(clientPath + File.separator + filename);
				if (file.exists()) {
					file.delete();
					System.out.println(clientPath);
					System.out.println(file.getName());
					return "File " + filename + " deleted from " + clientPath.toString();
				} else {
					return "File is not exists";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	public String readMsg(DataInputStream in) throws IOException {
		StringBuilder sbr = new StringBuilder();
		while (true) {
			byte[] buffer = new byte[512];
			int size = in.read(buffer);
			sbr.append(new String(buffer, 0, size));
			if (sbr.toString().endsWith("end")) {
				break;
			}
		}
		System.out.println(sbr.toString());


		if ((sbr.toString().startsWith("1"))&&(sbr.toString().split("\n")[0].equals("1"))){
			setServerPath(Path.of(sbr.substring(2,sbr.toString().length()-4)));
			System.out.println(sbr.length());



			System.out.println(sbr.substring((2+sbr.toString().split("\n")[1].length()),sbr.toString().length()-4));
			return sbr.substring(0,1);
		}

		return sbr.substring(0, sbr.toString().length() - 4);
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
