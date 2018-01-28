package fun.dircon.mainapp;

import biz.ui.launchers.generic.SaveableAppLauncher;
import javafx.stage.Stage;

public class DirConApp extends SaveableAppLauncher<DirConController>{

	@Override
	public String getPathtoFXML() {
		return "/resources/dirConGUI.fxml";
	}

	@Override
	public String getStageTitle() {
		return "DirectoryConsolidator";
	}

	@Override
	public void init() {
		
	}
	
	@Override
	public void start(Stage stage){
		super.start(stage);
	}

}
