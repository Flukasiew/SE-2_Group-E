package com.pw.player;
import com.pw.player.model.*;

public class PlayerApp {

	public static void main(String host, int port) {
		Player player = new Player(host, port);
		player.listen();
	}
}