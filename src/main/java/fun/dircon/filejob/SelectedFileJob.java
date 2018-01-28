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
		if(keepOriginalFile && exts.length == 0) {
			super.getExtractFiles();
			System.out.println("copying all files");
			super.executeCopy().start();
			
		}else if(keepOriginalFile && exts.length != 0) {
			super.consolidateFiles();
			System.out.println("copying select files");
			super.executeCopy().start();
			
		}else if(!keepOriginalFile && exts.length == 0) {
			super.getExtractFiles();
			System.out.println("Moving all files");
			super.executeMove().start();
			
		}else {
			super.consolidateFiles();
			System.out.println("Moving select files");
			super.executeMove().start();			
		}
	}

}
