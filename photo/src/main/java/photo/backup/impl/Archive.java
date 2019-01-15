package photo.backup.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import photo.backup.AbstractExec;
import photo.backup.common.CommonUtil;

public class Archive extends AbstractExec {

	private String rootDir;

	private String backupDir;

	private int archiveMonth;

	public void exec(String[] args) {
		// 事前セットアップ
		rootDir = conf.getProperty("RootDirectory");
		backupDir = conf.getProperty("BackupDirectory");
		archiveMonth = conf.getPropertyIntValue("ArchiveMonth");
		// ディレクトリのチェック
		if (!CommonUtil.checkDirAuth(rootDir)) {
			throw new IllegalArgumentException("処理対象ディレクトリが存在しません：" + rootDir);
		} else if (!CommonUtil.checkDirAuth(backupDir)) {
			throw new IllegalArgumentException("バックアップディレクトリが存在しません：" + backupDir);
		}
		try {
			logger.info("【Archive処理開始】");
			for (Path yearPath : Files.list(Paths.get(rootDir)).collect(Collectors.toList())) {
				// 年ディレクトリに対する処理
				if (Files.isDirectory(yearPath) && !Files.isSymbolicLink(yearPath) && CommonUtil.isDigits(yearPath)) {
					// 月日ディレクトリに対する処理
					for (Path dayPath : Files.list(yearPath).collect(Collectors.toList())) {
						// 月日ディレクトリの形態チェック
						if (Files.isDirectory(dayPath) && !Files.isSymbolicLink(dayPath)) {
							// 月日ディレクトリの日付チェック
							if (dateCheck(yearPath.getFileName().toString(), dayPath.getFileName().toString())) {
								Path bkPath = Paths.get(backupDir, dayPath.getFileName().toString());
								// バックアップ確認
								if (CommonUtil.checkBackup(dayPath, bkPath)) {
									// ディレクトリ削除とシンボリックリンク生成
									execArchive(dayPath, bkPath);
								}
							}
						}
					}
				}
			}
			logger.info("【Archive処理完了】");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 指定した月数以前のディレクトリか確認します
	 *
	 * @param year yyy
	 * @param day mmdd
	 * @return 処理対象でtrue
	 */
	private boolean dateCheck(String year, String day) throws ParseException {
		// 日付形式チェック
		if (!day.matches("^[0-9]{1,2}+[^0-9]*[0-9]{1,2}.*$")) {
			return false;
		}
		// 処理対象日の形式変換
		int monthNum;
		int dateNum;
		if (CommonUtil.isDigits(day)) {
			monthNum = Integer.parseInt(day.substring(0, 2));
			dateNum = Integer.parseInt(day.substring(2));
		} else {
			String[] splitDay = day.split("[^0-9]+");
			monthNum = Integer.parseInt(splitDay[0]);
			dateNum = Integer.parseInt(splitDay[1]);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String parseDay = String.format("%s%02d%02d", year, monthNum, dateNum);
		Date targetDate = sdf.parse(parseDay);
		// 本日日付-処理対象月数
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.MONTH, -archiveMonth);
		// 結果判定
		return targetDate.before(cal.getTime());
	}

	/**
	 * Archive化処理を実行します
	 *
	 * @param targetPath 処理対象パス
	 * @param bkPath バックアップディレクトリパス
	 */
	private void execArchive(Path targetPath, Path bkPath) throws IOException {
		// 削除するファイルを一時的に移動
		String targetUri = targetPath.toString();
		Path tmpPath = Paths.get(targetUri + "-bk");
		Files.move(targetPath, tmpPath);
		// シンボリックリンク作成
		try {
			Files.createSymbolicLink(Paths.get(targetUri), bkPath);
			logger.info("シンボリックリンク作成：", targetPath.toString(), "->", bkPath.toString());
		} catch (Exception e) {
			// シンボリックリンク作成に失敗したときは元の状態に戻す
			Files.move(tmpPath, targetPath);
			throw e;
		}
		// メインディレクトリ削除
		CommonUtil.xremove(tmpPath);
		logger.info("ディレクトリ削除：", targetPath.toString());
	}
}
