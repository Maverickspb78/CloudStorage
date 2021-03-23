import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	Path serverPath = Paths.get("server");
	Path clientPath = Paths.get("client");


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
		JPanel panel2 = new JPanel();
		JPanel panalTa = new JPanel();
		panalTa.setSize(200,300);
		JTextArea taS = new JTextArea();
		JTextArea taC = new JTextArea();
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
		panel2.add(new JScrollPane(list));
		panel2.add(new JScrollPane(list2));
		panalTa.add(BorderLayout.WEST, taS);
		panalTa.add(BorderLayout.EAST, taC);
		fillList(myModel, serverPath);
		clientList(myModel2);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(taC.getText()));
			try	{
				fillList(myModel, serverPath);
				clientList(myModel2);
			} catch (IOException e){
				e.printStackTrace();
			}

		});
		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(taS.getText()));
			try	{
				fillList(myModel, serverPath);
				clientList(myModel2);
			} catch (IOException e){
				e.printStackTrace();
			}
		});
		removeButton.addActionListener(a -> {
			System.out.println(remove(taS.getText()));
			try {
				fillList(myModel, serverPath);
				clientList(myModel2);;
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		refreshButton.addActionListener(a -> {
			try {
				fillList(myModel, serverPath);
				clientList(myModel2);;
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		list.addListSelectionListener(a ->{
			taS.setText(list.getSelectedValue());
		});
		list2.addListSelectionListener(a ->{
			taC.setText(list2.getSelectedValue());
		});

	}

	private void clientList(DefaultListModel<String> myModel2){
		File file = new File("client");
		String[] files = file.list();
		myModel2.clear();
		if (files != null) {
			for (String fil:files){
				myModel2.addElement(fil);
			}
		}


	}

	private void fillList(DefaultListModel<String> myModel, Path path) throws IOException {
		List<String> list =  downloadFileList();
		myModel.clear();
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
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				String msg = ("upload\n" + filename + "\n");
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream inputStream = new BufferedInputStream(fis, 512);
				long size = file.length();
				System.out.println(size);
				out.write(msg.getBytes());
				System.out.println(inputStream.available());
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
			File file = new File("client" + File.separator + filename);
			if (!file.exists()) {
				file.createNewFile();

				out.write(("download\n" + filename+"\n").getBytes(StandardCharsets.UTF_8));
				out.flush();

				FileOutputStream fos = new FileOutputStream("client" + File.separator + filename);
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

	private String remove(String filename){
		try {

			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				String msg = "remove " + filename;
				out.write(msg.getBytes(StandardCharsets.UTF_8));
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
