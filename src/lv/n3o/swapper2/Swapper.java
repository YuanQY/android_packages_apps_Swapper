package lv.n3o.swapper2;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


public class Swapper extends Activity implements OnClickListener {

	ProgressDialog				dia;
	TextView					log;
	Thread						progress;
	SuCommander					su;
	Button						swapoff;
	Button						swapon;
	View						downloadBusybox;
	private Handler				handler					= new Handler() {
															@Override
															public void handleMessage(
																	Message msg) {
																log.append("\n->"
																		+ (String) msg.obj);
															}
														};

	private static final int	DIALOG_YES_NO_MESSAGE	= 999;

	private static final int	MENU_SWAP				= Menu.FIRST + 1;
	private static final int	MENU_CREATE				= Menu.FIRST + 2;
	private static final int	MENU_FORMAT				= Menu.FIRST + 3;
	private static final int	MENU_CONFIGURATION		= Menu.FIRST + 4;
	private static final int	MENU_SETTING			= Menu.FIRST + 5;
	private static final int	MENU_BUSYBOX			= Menu.FIRST + 6;
	private static final int	MENU_BUSYBOX_DOWNLOAD	= Menu.FIRST + 7;
	private static final int	MENU_BUSYBOX_REMOVE		= Menu.FIRST + 8;
	private static final int	MENU_INFO				= Menu.FIRST + 9;

	@Override
	public void onClick(View arg0) {
		final SwapperCommands sc = new SwapperCommands(this, handler);
		log.setText(R.string.waitings);
		if (arg0.equals(swapon)) {
			sc.swappiness();
			sc.swapOff();
			sc.swapOn();
		} else if (arg0.equals(swapoff)) {
			sc.swapOff();
		} else if (arg0.equals(downloadBusybox)) {
			final ProgressDialog pd = ProgressDialog.show(Swapper.this,
					getString(R.string.download_busybox_title),
					getString(R.string.download_busybox_summary), true);
			new Thread() {

				@Override
				public void run() {
					try {
						sc.prepareBusybox();
					} catch (Exception e) {
					}
					pd.dismiss();
				}
			}.start();
		} else {
			return;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadBusybox = new View(this.getApplicationContext());
		setContentView(R.layout.main);
		swapon = (Button) findViewById(R.id.SwapOn_32);
		log = (TextView) findViewById(R.id.LogText);
		swapon.setOnClickListener(this);
		swapoff = (Button) findViewById(R.id.SwapOff);
		swapoff.setOnClickListener(this);
		try {
			su = new SuCommander();
		} catch (IOException e) {
			log.append(e.getMessage());
			e.printStackTrace();
			// TODO: Here should be info about root :)
			finish();
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		SubMenu sm = menu.addSubMenu(Menu.NONE, MENU_SWAP, 0,
				R.string.menu_swap);
		sm.add(Menu.NONE, MENU_CREATE, 0, R.string.menu_create);
		sm.add(Menu.NONE, MENU_FORMAT, 0, R.string.menu_reformat);
		sm = menu.addSubMenu(Menu.NONE, MENU_CONFIGURATION, 0,
				R.string.menu_configuration);
		sm.add(Menu.NONE, MENU_SETTING, 0, R.string.menu_item_settings);
		sm.add(Menu.NONE, MENU_INFO, 0, R.string.menu_info);
		sm = menu.addSubMenu(Menu.NONE, MENU_BUSYBOX, 0, R.string.menu_busybox);
		sm.add(Menu.NONE, MENU_BUSYBOX_DOWNLOAD, 0,
				R.string.menu_busybox_download);
		sm.add(Menu.NONE, MENU_BUSYBOX_REMOVE, 0, R.string.menu_busybox_remove);
		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.hasSubMenu() == false) {
			String i = item.getTitle().toString();
			final int menuID = item.getItemId();

			final SwapperCommands sc = new SwapperCommands(this, handler);
			if (menuID == MENU_SETTING) {
				sc.swapOff();
				startActivity(new Intent(this, SwapperPreferences.class));
				return true;
			}
			log.setText(R.string.waitings);
			if (menuID == MENU_CREATE) {
				sc.swapOff();
				sc.createSwapFile();
			} else if (menuID == MENU_FORMAT) {
				sc.swapOff();
				sc.formatSwap();
			} else if (menuID == MENU_BUSYBOX_DOWNLOAD) {
				showDialog(DIALOG_YES_NO_MESSAGE);
			} else if (menuID == MENU_BUSYBOX_REMOVE) {
				sc.removeBusybox();
			} else if (MENU_INFO == menuID) {
				log.setText(R.string.swappiness_msg);
				log.append(su.exec_o("cat /proc/sys/vm/swappiness"));
				// structure
				// array listsp
				// total, used, free
				// TODO: it seems that some roms have different free output
				try {
					String[] free = su.exec_o("free").split("\n");
					ArrayList<String> mem = null;
					for (String line : free) {
						line = line.toLowerCase();
						if (line.indexOf("mem:") >= 0) {
							mem = new ArrayList<String>();
							String[] contents = line.split(" ");
							for (String s : contents) {
								s.replace(" ", "");
								if (s.length() > 0) {
									mem.add(s);
								}
							}

							log.append(String.format(
									getString(R.string.memory_info_msg),
									mem.get(1),
									mem.get(2), mem.get(3)));
						}
						
						if (line.indexOf("swap:") >= 0) {
							mem = new ArrayList<String>();
							String[] contents = line.split(" ");
							for (String s : contents) {
								s.replace(" ", "");
								if (s.length() > 0) {
									mem.add(s);
								}
							}
							log.append(String.format(
									getString(R.string.swap_info_msg),
									mem.get(1),
									mem.get(2), mem.get(3)));
						}
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					log.append(getString(R.string.read_error));
				}
			} else {
				return false;
			}
		}

		// Consume the selection event.
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
			case DIALOG_YES_NO_MESSAGE:
				return new AlertDialog.Builder(this).setTitle(
						R.string.busybox_install_dialog_title)
						.setPositiveButton(R.string.button_ok,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int whichButton) {
										Swapper.this.onClick(downloadBusybox);
									}
								}).setNegativeButton(R.string.button_cancel,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int whichButton) {

										// User clicked Cancel so do some stuff

										System.out.println("cancel clicked.");
									}
								}).create();
		}
		return null;
	}
}
