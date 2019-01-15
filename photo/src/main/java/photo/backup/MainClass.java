package photo.backup;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import photo.backup.common.ConfigLoader;
import photo.backup.common.Logger;
import photo.backup.impl.Archive;
import photo.backup.impl.Evacuation;

public class MainClass {

	private static final String ARCHIVE = "ARCHIVE";

	private static final String EVACUATION = "EVACUATION";

	private static ConfigLoader conf;

	private static Logger logger;

	public static void main(String[] args) {
		try {
			// プロパティファイル読み込み
			conf = ConfigLoader.getConfigLoader();
			// ロガー生成
			String logDir = conf.getProperty("LogDirectory");
			if (Files.notExists(Paths.get(logDir))) {
				Files.createDirectory(Paths.get(logDir));
			}
			logDir = logDir.endsWith("\\") ? logDir : (logDir + "\\");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String logfileUri = logDir + sdf.format(new Date()) + ".log";
			logger = Logger.setup(logfileUri);
			// 引数チェック
			if (args.length == 0) {
				throw new IllegalArgumentException("起動プログラムが指定されていません");
			}
			// コマンド実行
			String command = args[0].toUpperCase();

			if (ARCHIVE.equals(command)) {
				AbstractExec com = new Archive();
				com.exec(args);
			} else if (EVACUATION.equals(command)) {
				AbstractExec com = new Evacuation();
				com.exec(args);
			} else if ("CONFIGLIST".equals(command)) {
				System.out.println(conf);
			} else {
				throw new IllegalArgumentException("起動プログラム指定が不正です");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
	}
}