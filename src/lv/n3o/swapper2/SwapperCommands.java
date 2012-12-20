package lv.n3o.swapper2;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;


public class SwapperCommands extends Thread {

	static class command {

		String	title;
		String	command;

		public command(String title, String command) {
			super();
			this.title = title;
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public String getTitle() {
			return title;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	SharedPreferences			settings;
	Context						context;
	static SuCommander			su;
	String						swapPlace;
	String						swapPartPlace;
	String						busybox;
	int							swapSize;
	String						status;
	int							swappiness;
	static Handler				handler;
	boolean						swapPart;
	boolean						recreateSwap;
	boolean						remakeSwap;
	static ArrayList<command>	commands;
	static Thread				t;

	public SwapperCommands(Context c) {
		init_commands(c, (Handler) null);
	}

	public SwapperCommands(Context c, Handler h) {
		init_commands(c, h);
	}

	public void createSwapFile() {
		if (!swapPart) {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_create_file),
					"dd if=/dev/zero of=" + swapPlace + " bs=1048576 count="
							+ swapSize));
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_format_file), busybox
					+ " mkswap " + swapPlace));
		} else {
			SwapperCommands.commands.add(new command(
					context.getString(R.string.command_swap_partition_enable),
					"false"));
		}
	}

	public void formatSwap() {
		if (!swapPart) {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_format_file), busybox
					+ " mkswap " + swapPlace));
		} else {
			SwapperCommands.commands.add(new command(
					context.getString(R.string.command_format_partition),
					busybox + " mkswap "
							+ swapPartPlace));
		}

	}

	private void init_commands(Context c, Handler h) {
		SwapperCommands.handler = h;
		if (SwapperCommands.commands == null) {
			SwapperCommands.commands = new ArrayList<command>();
		}
		if (SwapperCommands.su == null) {
			try {
				SwapperCommands.su = new SuCommander();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (SwapperCommands.t == null) {
			SwapperCommands.t = new Thread(this);
			SwapperCommands.t.start();
		}
		context = c;
		settings = PreferenceManager.getDefaultSharedPreferences(c);
		try {
			SwapperCommands.su = new SuCommander();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		swapSize = Integer.parseInt(settings.getString("swapsize", "32"));
		swapPlace = settings.getString("swapplace", "/cache/swapfile.swp");
		swappiness = Integer.parseInt(settings.getString("swappiness", "10"));
		swapPart = settings.getBoolean("swappartenabled", false);
		swapPartPlace = settings.getString("swappartplace",
				"/dev/block/mmcblk0p3");
		recreateSwap = settings.getBoolean("recreateswap", true);
		remakeSwap = settings.getBoolean("remakeswap", true);
		busybox = settings.getString("busybox", "busybox");
	}

	public void prepareBusybox() {
		// SwapperCommands.commands.add(new command("Cleaning up old busybox",
		// "rm /data/local/bin/busybox"));
		// SwapperCommands.commands.add(new
		// command("Creating busybox directory",
		// "mkdir /data/local/bin"));
		SwapperCommands.commands.add(new command(context
				.getString(R.string.command_busybox_start_download),
				"true"));
		try {
			BusyboxComander.downloadBusybox();
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_busybox_downloaded),
					"true"));
			// SwapperCommands.commands.add(new
			// command("Copying busybox to /data/local/bin",
			// "dd if=/data/data/lv.n3o.swapper/busybox of=/data/local/bin/busybox"));
			SwapperCommands.commands.add(new command(
					context.getString(R.string.command_busybox_executable),
					"chmod 4777 /data/data/lv.n3o.swapper2/busybox"));
			SharedPreferences.Editor ed = settings.edit();
			ed.putString("busybox", "/data/data/lv.n3o.swapper2/busybox");
			ed.commit();
			busybox = settings.getString("busybox", "busybox");
			// SwapperCommands.commands.add(new command("Cleaning up", busybox
			// + " rm /data/data/lv.n3o.swapper/busybox"));
		} catch (Exception e) {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_busybox_download_fail),
					"false"));
			e.printStackTrace();
		}
	}

	public void removeBusybox() {
		SwapperCommands.commands.add(new command(context
				.getString(R.string.command_busybox_remove),
				"rm /data/data/lv.n3o.swapper2/busybox"));
		busybox = settings.getString("busybox", "busybox");
		if (busybox.equals("/data/local/bin/busybox")) {
			SharedPreferences.Editor ed = settings.edit();
			ed.putString("busybox", "busybox");
			ed.commit();
		}
		busybox = settings.getString("busybox", "busybox");
	}

	@Override
	public void run() {
		try {
			boolean done = false;
			while (SwapperCommands.t != null) {
				while (!SwapperCommands.su.isReady()) {
					Thread.sleep(100);
				}
				if (!SwapperCommands.commands.isEmpty()) {
					done = true;
					command c = SwapperCommands.commands.remove(0);
					SwapperCommands.su.exec(c.getCommand());
					while (!SwapperCommands.su.isReady()) {
						Thread.sleep(100);
					}
					if (SwapperCommands.handler != null) {
						Message m = Message.obtain();
						if (SwapperCommands.su.isSuccess()) {
							m.obj = c.getTitle()
									+ context.getString(R.string.ok);
						} else {
							m.obj = c.getTitle()
									+ context.getString(R.string.fail);
						}
						SwapperCommands.handler.sendMessage(m);
					}
					if (SwapperCommands.handler != null) {
						Log.i("Swapper",
								SwapperCommands.handler.toString()
										+ " EXEC "
										+ c.getTitle()
										+ " RESULT "
										+ String.valueOf(SwapperCommands.su
												.isSuccess()));
					} else {
						Log.i("Swapper",
								"H=null EXEC "
										+ c.getTitle()
										+ " RESULT "
										+ String.valueOf(SwapperCommands.su
												.isSuccess()));
					}
					String output = SwapperCommands.su.getOutput();
					if (output != null) {
						output = output.trim().replace("\n", "");
						if (output.length() > 2) {
							Log.d("Swapper", output);
						}

					}
					String errors = SwapperCommands.su.getErrors();
					if (errors != null) {
						Log.e("Swapper", errors);
					}

				} else {
					if ((SwapperCommands.handler != null) && done) {
						done = false;
						Message m = Message.obtain();
						m.obj = context.getString(R.string.all_down);
						SwapperCommands.handler.sendMessage(m);
					}
				}
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void swapOff() {
		if (swapPart) {
			SwapperCommands.commands.add(new command(
					context.getString(R.string.command_swap_partition_off),
					busybox + " swapoff "
							+ swapPartPlace));
		} else {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_swap_file_off),
					busybox + " swapoff " + swapPlace));
		}
		if (!swapPart && recreateSwap) {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_swap_file_remove),
					busybox + " rm " + swapPlace));
		}
	}

	public void swapOn() {
		if (!swapPart && recreateSwap) {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_create_file),
					"dd if=/dev/zero of=" + swapPlace + " bs=1048576 count="
							+ swapSize));
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_change_permission),
					busybox + " chmod 600 " + swapPlace));

		}

		if (remakeSwap) {
			formatSwap();
		}

		if (swapPart) {
			SwapperCommands.commands.add(new command(
					context.getString(R.string.command_enable_swap_partition),
					busybox + " swapon "
							+ swapPartPlace));
		} else {
			SwapperCommands.commands.add(new command(context
					.getString(R.string.command_enable_swap_file),
					busybox + " swapon " + swapPlace));

		}
	}

	public void swappiness() {
		SwapperCommands.commands.add(new command(context
				.getString(R.string.command_setting_swappiness), "echo "
				+ swappiness + " > /proc/sys/vm/swappiness"));
	}
}
