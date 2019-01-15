package photo.backup.common;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger implements Closeable {

	private static Logger logger = new Logger();

	private static FileOutputStream fo;

	private static OutputStreamWriter os;

	private static BufferedWriter bw;

	private static final String LOG_FORMAT = "[%s][%s]%s";

	private static final String LOGLEVEL_WARN = "WARN";

	private static final String LOGLEVEL_INFO = "INFO";

	private Logger() {
	}

	/**
	 * Logger取得
	 *
	 * @return Loggerインスタンス
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * Logger初期設定
	 *
	 * @param filePath ログファイル
	 * @return Loggerインスタンス
	 */
	public static Logger setup(String filePath) {
		if (fo == null || os == null || bw == null) {
			try {
				fo =  new FileOutputStream(filePath, true);
				os = new OutputStreamWriter(fo,"utf-8");
				bw = new BufferedWriter(os);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return logger;
	}

	/**
	 * ログを追記する
	 *
	 * @param level ログレベル
	 * @param msg メッセージ
	 */
	public void writeLog(String level, String... msg) {
		try {
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String str = String.format(LOG_FORMAT, sdf.format(now), level, String.join(" ", msg));
			bw.write(str);
			bw.write("\r\n");
			bw.flush();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * ログレベルWARNを追記する
	 *
	 * @param msg メッセージ
	 */
	public void warn(String... msg) {
		writeLog(LOGLEVEL_WARN, msg);
	}

	/**
	 * ログレベルINFOを追記する
	 *
	 * @param msg メッセージ
	 */
	public void info(String... msg) {
		writeLog(LOGLEVEL_INFO, msg);
	}

	/**
	 * クローズ処理
	 */
	public void close() {
		try {
			if (fo != null) {
				fo.close();
			}
		} catch (IOException e) {
		}
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
		}
		try {
			if (bw != null) {
				bw.close();
			}
		} catch (IOException e) {
		}
	}
}
