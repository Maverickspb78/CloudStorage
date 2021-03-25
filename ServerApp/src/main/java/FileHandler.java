
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;



import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Client connected: " + ctx.channel().remoteAddress());
	}


	@Override
	public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {


		String filename = "";
		System.out.println("Message:\n" + msg);
		String command = msg.split("\n")[0];
		if (command.startsWith("list-files")) {
			Path serverPath = Path.of(msg.split("\n")[1]);
			File file = new File(serverPath.toString());
			if (Files.isDirectory(Paths.get(serverPath.toString()))) {
				if (file.getName().equals("...")) {
					file = new File(file.getParent());
				}
				File[] files = file.listFiles();
				StringBuffer sb = new StringBuffer();
				for (File f : files) {
					sb.append(f.getName() + "\n");
				}
				sb.append("end");
				ctx.writeAndFlush(sb.toString());
			}
			else {
				String[] p = serverPath.toString().split("\\\\");
				if (p.length > 1) {
					String pah = "";
					for (int i = 0; i < p.length - 1; i++) {
						pah += p[i] + "\\";
					}

					serverPath = Paths.get(pah);
					file = new File(serverPath.toString());
					File[] files = file.listFiles();
					StringBuffer sb = new StringBuffer();
					sb.append("false\n");
					for (File f : files) {
						sb.append(f.getName() + "\n");
					}
					sb.append("end");
					ctx.writeAndFlush(sb.toString());
				}
			}


		} else if (command.startsWith("remove")) {
			filename = msg.split(" ")[1];
			remove(ctx, filename);
		} else if (command.startsWith("upload")) {
			filename = msg.split("\n")[1];
			int length = command.length()+ filename.length() + 2;
			System.out.println(length);
			upload(ctx, msg, length);

		} else if (command.startsWith("download")) {
			filename = msg.split("\n")[1];
			download(ctx, filename);
		} else {
			System.out.println("Chanel closed");
			ctx.channel().closeFuture();
			ctx.channel().close();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
	}

	public void remove(ChannelHandlerContext ctx, String filename) throws IOException {
		try {
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				if (file.delete()) {
					ctx.writeAndFlush("File " + filename + " deleted from server\nend");
				} else {
					ctx.writeAndFlush("The file " + filename + " is not deleted from the server\nend");
				}
			}

		} catch (Exception e) {
			ctx.writeAndFlush("ERROR\nend".getBytes(StandardCharsets.UTF_8));
		}
	}

	public void upload(ChannelHandlerContext ctx, String msg, int length) throws IOException {


		String filename = msg.split("\n")[1];
		try {

			File file = new File("server" + File.separator + filename);

			if (!file.exists()) {
				file.createNewFile();
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
//				msg = ctx.channel().read().alloc().buffer().toString();
//				bufferedWriter.write(msg);
				bufferedWriter.write(msg.substring(length));
				bufferedWriter.close();
				ctx.writeAndFlush("File " + filename + " coped from server\nend");
			} else {
				ctx.writeAndFlush("File " + filename + " already exists\nend");
			}
		} catch (Exception e) {
			ctx.writeAndFlush("ERROR\nend");
		}
	}

	public void download(ChannelHandlerContext ctx, String filename) throws IOException {
		try {
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

				String m ="";
				while (bufferedReader.ready()) {
					m = m + bufferedReader.readLine() + "\n";
				}
				System.out.println(m);
				ctx.writeAndFlush(m);
				ctx.writeAndFlush("File " + filename + " coped from server\nend");
				bufferedReader.close();

			}
			else {
				ctx.writeAndFlush("File " + filename + " already exists\nend");
			}
		} catch (Exception e) {
			ctx.writeAndFlush("ERROR\n");
		}


	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		//cause.printStackTrace();
		ctx.close();
	}


}


