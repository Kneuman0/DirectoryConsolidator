package fun.dircon.filejobdisplay;

import biz.ui.features.InformedProgressBar;
import fun.dircon.filejob.FileJob;
import fun.dircon.filejob.SelectedFileJob;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class FileJobDisplayer extends InformedProgressBar{
	
	protected Label progressInfoLabel;
	protected SelectedFileJob job;
	
	public FileJobDisplayer(SelectedFileJob job) {
		job.setProgressListener(new DisplayNotification());
		this.progressInfoLabel = new Label();
		progressInfoLabel.setMinHeight(80);
		super.getChildren().add(progressInfoLabel);
		this.job = job;
	}
	
	public Label getProgressInfoLabel() {
		return progressInfoLabel;
	}



	public void setProgressInfoLabel(Label progressInfoLabel) {
		this.progressInfoLabel = progressInfoLabel;
	}



	public SelectedFileJob getJob() {
		return job;
	}



	public void setJob(SelectedFileJob job) {
		this.job = job;
	}



	/**
	 * Allows the GUI to display the progress of the move from another thread
	 * @author karottop
	 *
	 */
	public class DisplayNotification implements FileJob.FileJobProgressListener{

		@Override
		public void notifyProgress(String progressInfo) {
			Platform.runLater(new DisplayLabelThread(progressInfoLabel, progressInfo));
			Platform.runLater(new UpdateProgress(job.getPercentComplete().get()/100.0));
		}

		@Override
		public void notifyCurrentFile(String currentFileInfo) {
			Platform.runLater(new DisplayLabelThread(progressMessage, currentFileInfo));
		}
		
	}
	
}
