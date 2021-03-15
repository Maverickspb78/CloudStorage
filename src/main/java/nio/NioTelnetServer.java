package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NioTelnetServer {
	private final ByteBuffer buffer = ByteBuffer.allocate(512);
	// touch (имя файла) - создание файла
	// mkdir (имя директории) - создание директории
	// cd (path) - перемещение по дереву папок
	// rm (имя файла или папки) - удаление объекта
	// copy (src, target) - копирование файла
	// cat (имя файла) - вывод в консоль содержимого

	public static final String LS_COMMAND = "\tls          view all files from current directory\n\r";
	public static final String MKDIR_COMMAND = "\tmkdir       create directory\n\r";
	public static final String CHANGE_NICKNAME_COMMAND = "\tnick        change nickname\n\r";
	public static final String TOUCH_COMMAND = "\ttouch       create file\n\r";
	public static final String CD_COMMAND = "\tcd          moving through the folder tree\n\r";
	public static final String RM_COMMAND = "\trm          deleting an object\n\r";
	public static final String COPY_COMMAND = "\tcopy        copy file\n\r";
	public static final String CAT_COMMAND = "\tcat       console output\n\r";

	private Map<String, SocketAddress> clients = new HashMap<>();
	private String dir = "server";


	public NioTelnetServer() throws IOException {
		ServerSocketChannel server = ServerSocketChannel.open(); // открыли
		server.bind(new InetSocketAddress(1234));
		server.configureBlocking(false); // ВАЖНО
		Selector selector = Selector.open();
		server.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server started");
		while (server.isOpen()) {
			selector.select();
			var selectionKeys = selector.selectedKeys();
			var iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				var key = iterator.next();
				if (key.isAcceptable()) {
					handleAccept(key, selector);
				} else if (key.isReadable()) {
					handleRead(key, selector);
				}
				iterator.remove();
			}
		}
	}

	private void handleRead(SelectionKey key, Selector selector) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		SocketAddress client = channel.getRemoteAddress();
		String nickname = "";
		String fileName = "";
		String dirName = "";
		String target = "";
		String dirNameDell = "";

		Path path = Path.of("");
		int readBytes = channel.read(buffer);
		if (readBytes < 0) {
			channel.close();
			return;
		} else if (readBytes == 0) {
			return;
		}

		buffer.flip();
		StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining()) {
			sb.append((char) buffer.get());
		}
		buffer.clear();

		// TODO: 05.03.2021
		// touch (имя файла) - создание файла
		// mkdir (имя директории) - создание директории
		// cd (path) - перемещение по дереву папок
		// rm (имя файла или папки) - удаление объекта
		// copy (src, target) - копирование файла
		// cat (имя файла) - вывод в консоль содержимого

		if (key.isValid()) {
			String command = sb.toString()
					.replace("\n", "")
					.replace("\r", "");
			if ("--help".equals(command)) { // help command
				sendMessage(LS_COMMAND, selector, client);
				sendMessage(MKDIR_COMMAND, selector, client);
				sendMessage(CHANGE_NICKNAME_COMMAND, selector, client);
				sendMessage(TOUCH_COMMAND, selector, client);
				sendMessage(CD_COMMAND, selector, client);
				sendMessage(RM_COMMAND, selector, client);
				sendMessage(COPY_COMMAND, selector, client);
				sendMessage(CAT_COMMAND, selector, client);
			} else if (command.startsWith("nick ")) { // nick (nickname) - смена никнайма
				nickname = command.split(" ")[1];
				clients.put(nickname, client);
				System.out.println("Client [" + client.toString() + "] changes nickname on [" + nickname + "]");
			} else if ("ls".equals(command)) { // ls - показывает папки\файлы по текущему пути (переменная dir)
				sendMessage(getFilesList().concat("\n\r"), selector, client);

			} else if (command.startsWith("touch")) { // touch (имя файла) - создание файла
				fileName = command.split(" ")[1];
				path = Path.of(dir + File.separator + fileName);
				System.out.println(fileName + " " + path);
				if (!Files.exists(path)) {
					Files.createFile(path);
				}
			} else if (command.startsWith("mkdir")) { // mkdir (имя директории) - создание директории
				dirName = command.split(" ")[1];
				path = Path.of(dir + File.separator + dirName);
				System.out.println(dirName + " " + path);
				if (!Files.exists(path)) {
					Files.createDirectory(path);
				}

			} else if (command.startsWith("cd")) {  //cd (path) - перемещение по дереву папок( записывается в переменную
													// dir. Нуно вводить полный путь server\...)
				setDir(command.split(" ")[1]);
				System.out.println(dir);


			} else if (command.startsWith("rm")) { // rm (имя файла или папки) - удаление объекта
				dirName = command.split(" ")[1]; // пишем имя директории с именем файла\директории
				if (dirName.contains(".")) {

					char[] str = dirName.toCharArray(); // преобразуем в масив символов
					for (int i = str.length - 1; i > 0; i--) { //перебираем масив str с конца
						if (str[i] == '\\') { // ищем последний символ \ (после него должно идти имя файла)
							int index = i + 1; // возвращаем на 1 символ вперед(т.к. идет перебор с конца)
							while (index < str.length) { // записываем имя файла в fileName
								fileName += str[index];
								index++;
							}
							dirName = dirName.substring(0, i); // обрезаем dirName - fileName
							break;
						}
					}
					Files.delete(Path.of(dirName, fileName));

				} else {
					char[] str = dirName.toCharArray(); // преобразуем в масив символов
					for (int i = str.length - 1; i > 0; i--) { //перебираем масив str с конца
						if (str[i] == '\\') { // ищем последний символ \ (после него должно идти имя директории)
							int index = i + 1; // возвращаем на 1 символ вперед(т.к. идет перебор с конца)
							while (index < str.length) { // записываем имя директории в fileName
								dirNameDell += str[index];
								index++;
							}
							dirName = dirName.substring(0, i); // обрезаем dirName - dirNameDell
							break;
						}
					}
					Files.delete(Path.of(dirName, dirNameDell));
				}


			} else if (command.startsWith("copy")) { // copy (src, target) - копирование файла
				dirName = command.split(" ")[1]; // пишем имя директории с именем файла
				char[] str = dirName.toCharArray(); // преобразуем в масив символов
				for (int i = str.length - 1; i > 0; i--) { //перебираем масив str с конца
					if (str[i] == '\\') { // ищем последний символ \ (после него должно идти имя файла)
						int index = i + 1; // возвращаем на 1 символ вперед(т.к. идет перебор с конца)
						while (index < str.length) { // записываем имя файла в fileName
							fileName += str[index];
							index++;
						}
						dirName = dirName.substring(0, i); // обрезаем dirName - fileName
						break;
					}
				}

				target = command.split(" ")[2];
				Path pathTarget = Path.of(target + File.separator + fileName);
				path = Files.copy(Path.of(dirName + File.separator + fileName), pathTarget);


			} else if (command.startsWith("cat")) { // cat (имя файла) - вывод в консоль содержимого
				dirName = command.split(" ")[1]; // пишем имя директории с именем файла
				char[] str = dirName.toCharArray(); // преобразуем в масив символов
				for (int i = str.length - 1; i > 0; i--) { //перебираем масив str с конца
					if (str[i] == '\\') { // ищем последний символ \ (после него должно идти имя файла)
						int index = i + 1; // возвращаем на 1 символ вперед(т.к. идет перебор с конца)
						while (index < str.length) { // записываем имя файла в fileName
							fileName += str[index];
							index++;
						}
						dirName = dirName.substring(0, i); // обрезаем dirName - fileName
						break;
					}
				}
				path = Path.of(dirName + File.separator + fileName);
				Files.readAllLines(path).forEach(System.out::println);

			} else if ("exit".equals(command)) {
				System.out.println("Client logged out. IP: " + channel.getRemoteAddress());
				channel.close();
				return;
			} else System.out.println("Wrong command");
		}

		for (Map.Entry<String, SocketAddress> clientInfo : clients.entrySet()) {
			if (clientInfo.getValue().equals(client)) {
				nickname = clientInfo.getKey();
			}
			sendName(channel, nickname);
		}


	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}



	private void sendName(SocketChannel channel, String nickname) throws IOException {
		if (nickname.isEmpty()) {
			nickname = channel.getRemoteAddress().toString();
		}
		channel.write(
				ByteBuffer.wrap(nickname
						.concat(">: ")
						.getBytes(StandardCharsets.UTF_8)
				)
		);
	}

	private String getFilesList() {
		return String.join("\t", new File(dir).list());
	}

	private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) {
					((SocketChannel) key.channel())
							.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
				}
			}
		}
	}

	private void handleAccept(SelectionKey key, Selector selector) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);
		System.out.println("Client accepted. IP: " + channel.getRemoteAddress());
		channel.register(selector, SelectionKey.OP_READ, "some attach");
		channel.write(ByteBuffer.wrap("Hello user!\n\r".getBytes(StandardCharsets.UTF_8)));
		channel.write(ByteBuffer.wrap("Enter --help for support info\n\r".getBytes(StandardCharsets.UTF_8)));
		sendName(channel, "");
	}

	public static void main(String[] args) throws IOException {
		new NioTelnetServer();
	}
}
