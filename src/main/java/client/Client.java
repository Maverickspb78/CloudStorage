package client;


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

//	private DefaultListModel<String> listModel;



	public Client() throws IOException {
		socket = new Socket("localhost", 1234);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {

//		listModel = new DefaultListModel<String>();

		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		JTextArea ta = new JTextArea();

		JList<String> list = new JList<>();
		DefaultListModel<String> myModel = new DefaultListModel<>();
		list.setModel(myModel);

		// TODO: 02.03.2021
		// list file - JList

//		File rootFolder = new File("server");
//		String[] filesTemp = rootFolder.list();
//		for (int i=0; i < filesTemp.length; i++){
//			listModel.addElement(filesTemp[i]);
//		}
//		JList<String> list = new JList<String>(listModel);

		JButton uploadButton = new JButton("Upload");
		uploadButton.setSize(20,40);

		JButton downloadButton = new JButton("download");
		downloadButton.setSize(20,40);

		JButton removeButton = new JButton("remove");
		removeButton.setSize(20,40);

		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);

		fillList(myModel);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
//			if (listModel.contains(ta.getText())){
//
//			}else {
//				listModel.addElement(ta.getText());
//				System.out.println("Новый :" + listModel);
//			}
		});

		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(ta.getText()));
		});
		removeButton.addActionListener(a -> {
			System.out.println(remove(ta.getText()));

//			for (int i = 0; i < listModel.size(); i++) {
//				System.out.println("ta :" + ta.getText());
//
//
//				if (listModel.getElementAt(i).contains(ta.getText())){
//					System.out.println("Сработало перед удалением: " + listModel.getElementAt(i));
//					listModel.remove(i);
//					System.out.println("Новый :" + listModel);
//					break;
//
//
//				}
//
//
//			}




		});
		list.addListSelectionListener(a ->{
			ta.setText(list.getSelectedValue());
		});
	}

	private void fillList(DefaultListModel<String> myModel) {
		List<String> list =  downloadFileList();
		myModel.clear();
		for (String filename : list) {
			myModel.addElement(filename);
		}
	}

	private List<String> downloadFileList() {
		List<String> list = new ArrayList<String>();
		try {
			StringBuilder sb = new StringBuilder();
			out.write("list-files".getBytes(StandardCharsets.UTF_8));
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

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
				return status;
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
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("download");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
				return status;
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
				out.writeUTF("remove");
				out.writeUTF(filename);
				out.flush();
				String status = in.readUTF();
				return status;
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
