package fun.dircon.mainapp;

import java.io.File;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class DirConController extends FileUtilController {
	
    @FXML
    private Button consolidateButton;

    @FXML
    private TextField extField, pathField, pathFieldExport;

    @FXML
    private CheckBox extCheckBox;
    
    @FXML
    private Label currentProgress, currentFile;
    
    private File parent;
    
    private File exportDir;
    
    @FXML
    private RadioButton copyRadioButton;
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
	
	public void browseForParent() {
		File parent = super.requestDirectory("Select Parent Directory", null);
		if (parent == null) return;
		pathField.setText(parent.getAbsolutePath());
		this.parent = parent;
	}
	
	public void selectExportDir() {
		File exportDir = super.requestDirectory("Select Export Directory", null);
		if (exportDir == null) return;
		pathFieldExport.setText(exportDir.getAbsolutePath());
		this.exportDir = exportDir;
	}
	
	/**
	 * This method will either move or copy the files recursively 
	 * from one directory and put them at the root of another. The use can select whether 
	 * they want these files moved or copied and they can choose to only copy files with specific extensions
	 */
	public void consolidateParent() {
		/**
		 * copy all files in parent directory and sub directories with a particular extension
		 */
		if(extCheckBox.isSelected() && copyRadioButton.isSelected()) {
			// get selected extensions
			String[] exts = extField.getText().split("[,]");
			
			// create new file job and register listener
			FileJob job = new FileJob(parent, exportDir, new DisplayNotification(), exts);
			if(exportDir == null) return;
			
			// run the job in a new thread
			Platform.runLater(new DisplayLabelThread(currentFile, "Exporting Files"));
			Thread copy = job.executeCopy();
			copy.start();
		/**
		 * copy all files in parent directory and sub directories
		 */
		}else if(!extCheckBox.isSelected() && copyRadioButton.isSelected()){
			// create new file job and register listener
			FileJob job = new FileJob(parent, exportDir, new DisplayNotification(), new String[0]);
			if(exportDir == null) return;		
			
			// run the job in a new thread
			Platform.runLater(new DisplayLabelThread(currentFile, "Exporting Files"));
			Thread copy = job.executeCopy();
			copy.start();
		
		/**
		 * move all files in parent directory and sub directories with a particular extension
		 */
		}else if(extCheckBox.isSelected() && !copyRadioButton.isSelected()) {
			// get selected extensions
			String[] exts = extField.getText().split("[,]");
			
			// create new file job and register listener
			FileJob job = new FileJob(parent, exportDir, new DisplayNotification(), exts);
			if(exportDir == null) return;
			
			// run the job in a new thread
			Platform.runLater(new DisplayLabelThread(currentFile, "Exporting Files"));
			Thread move = job.executeMove();
			move.start();
		/**
		 * copy all files in parent directory and sub directories
		 */
		}else {
			// create new file job and register listener
			FileJob job = new FileJob(parent, exportDir, new DisplayNotification(), new String[0]);
			if(exportDir == null) return;		
			
			// run the job in a new thread
			Platform.runLater(new DisplayLabelThread(currentFile, "Exporting Files"));
			Thread move = job.executeMove();
			move.start();
		}

	}
	
	/**
	 * Allows the GUI to display the progress of the move from another thread
	 * @author karottop
	 *
	 */
	public class DisplayNotification implements FileJob.FileJobListener{

		@Override
		public void notifyProgress(String progressInfo) {
			Platform.runLater(new DisplayLabelThread(currentProgress, progressInfo));
		}

		@Override
		public void notifyCurrentFile(String currentFileInfo) {
			Platform.runLater(new DisplayLabelThread(currentFile, currentFileInfo));
		}
		
		
	}

}
