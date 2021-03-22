import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
		JTextArea ta = new JTextArea();
		Icon iconRefresh = new ImageIcon("icon\\refresh2.png");


		JList<String> list = new JList<>();
		list.setModel(myModel);

		JButton uploadButton = new JButton("Upload");
		uploadButton.setSize(20,40);

		JButton downloadButton = new JButton("download");
		downloadButton.setSize(20,40);

		JButton removeButton = new JButton("remove");
		removeButton.setSize(20,40);

		JButton refreshButton = new JButton(iconRefresh);
		refreshButton.setSize(20,20);


		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);
		panel.add(refreshButton);

		fillList(myModel);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			try {
				System.out.println(sendFile(ta.getText()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(ta.getText()));
		});
		removeButton.addActionListener(a -> {
			System.out.println(remove(ta.getText()));
			try {
				fillList(myModel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		refreshButton.addActionListener(a -> {
			try {
				fillList(myModel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		list.addListSelectionListener(a ->{
			ta.setText(list.getSelectedValue());
		});

	}

	private void fillList(DefaultListModel<String> myModel) throws IOException {
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
			String fileString = sb.substring(0, sb.toString().length() - 4);
			list = Arrays.asList(fileString.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String sendFile(String filename) throws IOException {
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
		out.flush();

		return sbr.substring(0, sbr.toString().length() - 4);
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
