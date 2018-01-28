package fun.dircon.filejobdisplay;

import biz.ui.launchers.generic.PopupLauncher;
import fun.dircon.filejob.FileJob;
import javafx.application.Platform;

@SuppressWarnings("rawtypes")
public class FileJobPopUpLauncher extends PopupLauncher{
	
	private FileJobDisplayer jobDisplayer;

	public FileJobPopUpLauncher(String title, FileJobDisplayer parent) {
		super(title, parent);
		this.jobDisplayer = parent;
		this.jobDisplayer.getJob().setJobCompleteListener(new JobFinishedTask());
	}
	
	@Override
	public void show() {
		super.show();
		jobDisplayer.getJob().executeJob();
	}
	
	public class JobFinishedTask implements FileJob.FileJobCompleteListener{

		@Override
		public void notifyJobComplete() {
			Platform.runLater(new CloseListener());
		}
		
	}
	
	public class CloseListener implements Runnable{

		@Override
		public void run() {
			getStage().close();
		}
		
	}

}
