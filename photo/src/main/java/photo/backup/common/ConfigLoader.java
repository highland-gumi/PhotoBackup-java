package photo.backup.common;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

	private static ConfigLoader loader = new ConfigLoader();

	private static Properties prop;

	private ConfigLoader() {
	}

	/**
	 * ConfigLoaderを取得する
	 *
	 * @return ConfigLoaderインスタンス
	 */
	public static ConfigLoader getConfigLoader() {
		if (prop == null) {

			String jarPath = System.getProperty("java.class.path");
			String configFile = jarPath.substring(0, jarPath.lastIndexOf("\\") + 1) + "setting.ini";
			// configFile = "D:\\backup\\setting.ini";
			prop = new Properties();
			try (Reader r = new BackslashEscape(Files.newBufferedReader(
					Paths.get(configFile), Charset.forName("UTF-8")))) {
				prop.load(r);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return loader;
	}

	/**
	 * キーからプロパティを取得する
	 *
	 * @param key 取得キー
	 * @return プロパティ
	 */
	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	/**
	 * キーからプロパティを取得する
	 *
	 * @param key 取得キー
	 * @return プロパティ
	 */
	public int getPropertyIntValue(String key) {
		return Integer.parseInt(prop.getProperty(key));
	}

	@Override
	public String toString() {
		return prop.toString();
	}

	/**
	 * \をエスケープするFileReader
	 */
	private static class BackslashEscape extends FilterReader {
		private boolean inInBackslash = false;

		public BackslashEscape(Reader r) {
			super(r);
		}

		@Override
		public int read() throws IOException {
			synchronized (lock) {
				return read1();
			}
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (len <= 0)
				return 0;
			int result = 0;
			synchronized (lock) {
				for (int i = 0; i < len; ++i) {
					int c = read1();
					if (c < 0)
						break;
					cbuf[off + i] = (char) c;
					++result;
				}
			}
			return result == 0 ? -1 : result;
		}

		@Override
		public long skip(long n) throws IOException {
			throw new IOException("not supported");
		}

		@Override
		public boolean ready() throws IOException {
			return inInBackslash;
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public void mark(int readAheadLimit) throws IOException {
			throw new IOException("not supported");
		}

		@Override
		public void reset() throws IOException {
			throw new IOException("not supported");
		}

		private int read1() throws IOException {
			if (inInBackslash) {
				inInBackslash = false;
				return '\\';
			}
			int result = super.read();
			if (result == '\\')
				inInBackslash = true;
			return result;
		}
	}
}
