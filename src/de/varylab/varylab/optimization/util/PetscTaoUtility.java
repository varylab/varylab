package de.varylab.varylab.optimization.util;

import de.jtem.jtao.Tao;

public class PetscTaoUtility {

	public static void initializePetscTao() {
		String[] taoCommand = new String[] {
				"-tao_nm_lamda", "0.01", 
				"-tao_nm_mu", "1.0"
			};
		Tao.Initialize("Tao Varylab", taoCommand, false);
	}
	
}
