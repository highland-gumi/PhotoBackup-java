package photo.backup.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {
	/**
	 * ディレクトリが書き込み可能であることを確認する
	 *
	 * @param paths チェック対象パス
	 * @return チェック結果
	 */
	public static boolean checkDirAuth(String... paths) {
		for (String str : paths) {
			Path path = Paths.get(str);
			if (Files.notExists(path) || !Files.isWritable(path)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 数値チェック
	 *
	 * @param str チェック対象
	 * @return チェック結果
	 */
	public static boolean isDigits(String str) {
		return str.matches("^[0-9]+$");
	}

	/**
	 * 数値チェック
	 *
	 * @param path ファイルのパス
	 * @return チェック結果
	 */
	public static boolean isDigits(Path path) {
		return isDigits(path.getFileName().toString());
	}

	/**
	 * バックアップファイルの有無を確認します
	 * 処理対象ディレクトリのファイルが全てバックアップディレクトリに存在すればOK
	 *
	 * @param targetPath 処理対象
	 * @param bkPath バックアップ
	 * @return バックアップ有無
	 */
	public static boolean checkBackup(Path targetPath, Path bkPath) throws IOException {
		if (Files.notExists(bkPath)) {
			Logger.getLogger().info("バックアップディレクトリなし：", bkPath.toString());
			return false;
		}
		List<Path> targetList = Files.list(targetPath).collect(Collectors.toList());
		List<Path> bkFileList = Files.list(bkPath).collect(Collectors.toList());
		for (Path targetFile : targetList) {
			if (Files.isDirectory(targetFile) && !Files.isSymbolicLink(targetFile)) {
				// ディレクトリの場合は内部を確認
				if (!checkBackup(targetFile,
						Paths.get(bkPath.toString(), targetFile.getFileName().toString()))) {
					// バックアップファイル不足
					return false;
				}
			} else {
				// ディレクトリ以外の場合はファイルチェック
				boolean exists = false;
				long fileSize = Files.size(targetFile);
				for (Path bkFile : bkFileList) {
					// ファイル名とファイルサイズを比較
					if (targetFile.getFileName().equals(bkFile.getFileName()) && fileSize == Files.size(bkFile)) {
						// バックアップあり
						exists = true;
						break;
					}
				}
				// バックアップ有無判定
				if (!exists) {
					// バックアップなし
					Logger.getLogger().info("バックアップファイル不足：", targetFile.toString());
					return false;
				}
			}
		}
		// 全ファイルチェック完了
		return true;
	}

	/**
	 * ディレクトリ内のファイルをすべてコピーする
	 *
	 * @param destPath コピー元
	 * @param srcPath コピー先
	 * @throws IOException copy失敗
	 */
	public static void xcopy(Path destPath, Path srcPath) throws IOException {
		Files.copy(destPath, srcPath);
		if (Files.isDirectory(destPath)) {
			for (Path childPath : Files.list(destPath).collect(Collectors.toList())) {
				xcopy(childPath, Paths.get(srcPath.toString(), childPath.getFileName().toString()));
			}
		}
	}

	/**
	 * ディレクトリ内のファイルとディレクトリを削除する
	 *
	 * @param path 削除するパス
	 * @throws IOException 削除失敗
	 */
	public static void xremove(Path path) throws IOException {
		for (Path child : Files.list(path).collect(Collectors.toList())) {
			Files.delete(child);
		}
		Files.delete(path);
	}

	public static String execProcess(String... command) {
		InputStream is = null;
		try {
			Process process = new ProcessBuilder(command).start();
			is = process.getInputStream();

			InputStreamReader isr = new InputStreamReader(is);

			BufferedReader reader = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			// コンソール出力される文字列の格納
			return builder.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static String execNetuse(String server, String user, String password) {
		return execProcess("net", "use", server, password, "/user:" + user);
	}
}
