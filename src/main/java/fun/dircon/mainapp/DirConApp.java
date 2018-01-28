package fun.dircon.mainapp;

import biz.ui.launchers.generic.AppLauncher;

public class DirConApp extends AppLauncher<DirConController>{

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

}
