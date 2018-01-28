package fun.dircon.filejob;

import java.io.File;

public class SelectedFileJob extends FileJob{
	
	protected boolean keepOriginalFile;

	public SelectedFileJob(File directory, File exportLocation,
			String[] exts, boolean keepOriginalFile) {
		super(directory, exportLocation, exts);
		this.keepOriginalFile = keepOriginalFile;
	}
	
	public void executeJob() {
		if(keepOriginalFile) {
			super.executeCopy().start();
			
		}else {
			super.executeMove().start();			
		}
	}

}
