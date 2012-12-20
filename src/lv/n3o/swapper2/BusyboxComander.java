package lv.n3o.swapper2;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class BusyboxComander {

	public static void downloadBusybox() throws MalformedURLException,
			IOException {
		URL u = new URL("http://swapper.n3o.lv/bin/busybox");
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod("GET");
		c.setDoOutput(true);
		c.connect();
		FileOutputStream f = new FileOutputStream(new File("/data/data/lv.n3o.swapper2/busybox"));
		InputStream in = c.getInputStream();
		byte[] buffer = new byte[1024];
		int len1 = 0;
		while ((len1 = in.read(buffer)) != -1) {
			f.write(buffer, 0, len1);
		}
		f.close();
		in.close();
	}
}
