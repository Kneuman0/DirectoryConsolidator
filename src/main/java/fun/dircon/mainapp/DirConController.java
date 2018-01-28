package fun.dircon.mainapp;

import java.io.File;

import biz.ui.controller.utils.ControllerUtils;
import fun.dircon.filejob.SelectedFileJob;
import fun.dircon.filejobdisplay.FileJobDisplayer;
import fun.dircon.filejobdisplay.FileJobPopUpLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class DirConController extends ControllerUtils{
	
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
		
		if(exportDir == null || parent == null) return;	
		// create new file job and register listener
		SelectedFileJob job = new SelectedFileJob(parent,
				exportDir, new String[0], copyRadioButton.isSelected());
		FileJobDisplayer jobDisplayer = new FileJobDisplayer(job);
		FileJobPopUpLauncher launcher = new FileJobPopUpLauncher("Exporting", jobDisplayer);
		System.out.println("Showing ProgressBar");
		launcher.show();
	}

}
