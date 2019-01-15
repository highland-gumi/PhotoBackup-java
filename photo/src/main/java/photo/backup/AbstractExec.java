package photo.backup;

import java.util.Date;

import photo.backup.common.ConfigLoader;
import photo.backup.common.Logger;

public abstract class AbstractExec {

	protected Date today;

	protected Logger logger;

	protected ConfigLoader conf;

	public AbstractExec(){
		today = new Date();
		logger = Logger.getLogger();
		conf = ConfigLoader.getConfigLoader();
	}

	public abstract void exec(String args[]);
}
