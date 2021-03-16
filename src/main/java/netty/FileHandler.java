package netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Client connected: " + ctx.channel().remoteAddress());
	}


	@Override
	public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {


		String filename = "";

		System.out.println("Message:\n" + msg);
//		System.out.println(msg.startsWith("list-files"));
		String command = msg.split("\n")[0];
//		System.out.println("command replace: " + command);
		if (command.equals("list-files")) {
			File file = new File("server");
			File[] files = file.listFiles();
			StringBuffer sb = new StringBuffer();
			for (File f : files) {
				sb.append(f.getName() + "\n");
			}
			sb.append("end");
			ctx.writeAndFlush(sb.toString());
		} else if (command.startsWith("remove")) {
			filename = msg.split(" ")[1];
			remove(ctx, filename);
		} else if (command.startsWith("upload")) {

			System.out.println("upload command: " + command);
			filename = msg.split("\n")[1];
			int length = command.length()+ filename.length() + 2;
			System.out.println("filename: " + filename);

			upload(ctx, msg, length);

		} else if (command.startsWith("download")) {
//			System.out.println("download command: " + command);
//			filename = command.split(" ")[1];
//			System.out.println("download command: " + filename);
//			ctx.write(("download" + "end").getBytes());
//			download(ctx, filename);
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

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		//cause.printStackTrace();
		ctx.close();
	}
}


