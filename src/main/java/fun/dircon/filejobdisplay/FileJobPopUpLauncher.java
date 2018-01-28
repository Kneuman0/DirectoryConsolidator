package fun.dircon.filejobdisplay;

import biz.ui.launchers.generic.PopupLauncher;
import fun.dircon.filejob.FileJob;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

@SuppressWarnings("rawtypes")
public class FileJobPopUpLauncher extends PopupLauncher{
	
	private FileJobDisplayer jobDisplayer;
	private NotifyJobFinishedListener notifyJobFinishedListener;

	public FileJobPopUpLauncher(String title, FileJobDisplayer parent) {
		super(title, parent);
		this.jobDisplayer = parent;
		this.jobDisplayer.getJob().setJobCompleteListener(new JobFinishedTask());
	}
	
	
	
	/**
	 * @return the jobDisplayer
	 */
	public FileJobDisplayer getJobDisplayer() {
		return jobDisplayer;
	}



	/**
	 * @param jobDisplayer the jobDisplayer to set
	 */
	public void setJobDisplayer(FileJobDisplayer jobDisplayer) {
		this.jobDisplayer = jobDisplayer;
	}



	/**
	 * @return the notifyJobFinishedListener
	 */
	public NotifyJobFinishedListener getNotifyJobFinishedListener() {
		return notifyJobFinishedListener;
	}



	/**
	 * @param notifyJobFinishedListener the notifyJobFinishedListener to set
	 */
	public void setNotifyJobFinishedListener(NotifyJobFinishedListener notifyJobFinishedListener) {
		this.notifyJobFinishedListener = notifyJobFinishedListener;
	}



	@Override
	public void show() {
		super.show();
		jobDisplayer.getJob().executeJob();
	}
	
	public class JobFinishedTask implements FileJob.FileJobCompleteListener{

		@Override
		public void notifyJobComplete() {
			notifyJobFinishedListener.onFinished(FileJobPopUpLauncher.this);
			Platform.runLater(new CloseListener());
		}
		
	}
	
	public class CloseListener implements Runnable{

		@Override
		public void run() {
			getStage().close();
		}
		
	}
	
	public class OnCloseRequest implements EventHandler<WindowEvent>{

		@Override
		public void handle(WindowEvent event) {
			jobDisplayer.job.setTerminate(true);
		}
		
	}
	
	public interface NotifyJobFinishedListener{
		void onFinished(FileJobPopUpLauncher fileJobPopulLauncher);
	}

}
