import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private int height = 300;
	private int width = 400;
	private DefaultListModel<String> myModel = new DefaultListModel<>();
	private DefaultListModel<String> myModel2 = new DefaultListModel<>();
	private String filNameServer = "";
	private String filNameClient = "";
	private Path serverPath = Paths.get("server");
	private Path clientPath = Paths.get("c:\\");




	public Client() throws IOException {
		socket = new Socket("localhost", 1234);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
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


		frame.getContentPane().add(BorderLayout.NORTH, panalTa);
		frame.getContentPane().add(BorderLayout.CENTER, panel2);
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);
		panel.add(refreshButton);
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
		list.addListSelectionListener(a ->{
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
				}
			});
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

	private void clientList(DefaultListModel<String> myModel2, Path clientPath){
		File file = new File(clientPath.toString());
		String[] files = file.list();
		myModel2.clear();
		myModel2.addElement("...");
		if (files != null) {
			for (String fil:files){
				myModel2.addElement(fil);
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
			out.write("list-files\n".getBytes(StandardCharsets.UTF_8));
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if (sb.toString().endsWith("end")) {
					break;
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
//				System.out.println(size);
				out.write(msg.getBytes());
//				System.out.println(inputStream.available());
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
						System.out.println("exit");
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
				System.out.println(serverPath);
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


		return sbr.substring(0, sbr.toString().length() - 4);
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
