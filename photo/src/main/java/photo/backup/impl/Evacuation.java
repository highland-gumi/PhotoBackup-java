package photo.backup.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import photo.backup.AbstractExec;
import photo.backup.common.CommonUtil;

public class Evacuation extends AbstractExec {

	private String rootDir;

	private String backupDir;

	private String evacuationDir;

	private int evacuationYear;

	public void exec(String[] args) {
		// 処理値設定
		rootDir = conf.getProperty("RootDirectory");
		backupDir = conf.getProperty("BackupDirectory");
		evacuationDir = conf.getProperty("EvacuationDirectory");
		evacuationYear = conf.getPropertyIntValue("EvacuationYear");
		// ディレクトリのチェック
		if (!CommonUtil.checkDirAuth(rootDir)) {
			throw new IllegalArgumentException("処理対象ディレクトリが存在しません：" + rootDir);
		} else if (!CommonUtil.checkDirAuth(backupDir)) {
			throw new IllegalArgumentException("バックアップディレクトリが存在しません：" + backupDir);
		} else if (!CommonUtil.checkDirAuth(evacuationDir)) {
			throw new IllegalArgumentException("退避ディレクトリが存在しません：" + evacuationDir);
		}
		try {
			// 処理開始
			logger.info("【Evacuation処理開始】");
			for (Path rootYearPath : Files.list(Paths.get(rootDir)).collect(Collectors.toList())) {
				// 年ディレクトリに対する処理
				if (Files.isDirectory(rootYearPath) && !Files.isSymbolicLink(rootYearPath)
						&& CommonUtil.isDigits(rootYearPath)) {
					int targetYear = Integer.parseInt(rootYearPath.getFileName().toString());
					if (targetYear <= evacuationYear) {
						// コピー先ディレクトリ取得
						Path copyPath = Paths.get(evacuationDir, rootYearPath.getFileName().toString());
						Path bkPath = Paths.get(backupDir, rootYearPath.getFileName().toString());
						if (checkBackup(bkPath, copyPath)) {
							Path rootPath = Paths.get(rootDir, copyPath.getFileName().toString());
							execReduction(rootPath, copyPath);
						}
					}
				}
			}
			logger.info("【Evacuation処理終了】");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private boolean checkBackup(Path bkPath, Path copyPath) throws Exception {
		if (!CommonUtil.checkDirAuth(copyPath.toString())) {
			// バックアップ先が存在しない場合はコピー
			CommonUtil.xcopy(bkPath, copyPath);
			logger.info("ファイル移動：", bkPath.toString(), "->", copyPath.toString());
		}
		// バックアップファイルのチェック
		return CommonUtil.checkBackup(bkPath, copyPath);
	}

	/**
	 * NASから移動する
	 *
	 * @param path バックアップ元のパス
	 * @throws Exception 例外
	 */
	private void execReduction(Path rootPath, Path copyPath) throws Exception {
		// 既存ファイル削除
		// メインディレクトリのディレクトリを移動
		Path tmpPath = Paths.get(rootPath.toString() + "-bk");
		Files.move(rootPath, tmpPath);
		try {
			// シンボリックリンク作成
			Files.createSymbolicLink(rootPath, copyPath);
			logger.info("シンボリックリンク作成：", rootPath.toString(), "->", copyPath.toString());
		} catch (Exception e) {
			// シンボリックリンク作成に失敗したときは元の状態に戻す
			Files.move(tmpPath, rootPath);
			throw e;
		}
		// ディレクトリ削除
		CommonUtil.xremove(tmpPath);
		logger.info("ディレクトリ削除：", rootPath.toString());
	}
}
