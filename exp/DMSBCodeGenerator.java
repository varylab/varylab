

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DMSBCodeGenerator {

	private static char[] symbols = {'A', 'B', 'C', 'D', 'E', 'F', 'W', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	
	public static void main(String[] args) throws Exception {
		Random rnd = new Random();
		Set<String> codes = new HashSet<String>();
		FileWriter w = new FileWriter("etc/dmsbCodes.csv");
		w.write("code\n");
		while (codes.size() < 300) {
			String code = "DMSB-";
			for (int j = 0; j < 6; j++) {
				code += symbols[rnd.nextInt(symbols.length)];
			}
			code += "-2013";
			if (codes.contains(code)) continue;
			codes.add(code);
			w.write(code);
			if (codes.size() < 300) {
				w.write("\n");
			}
			System.out.println(code);
		}
		w.flush();
		w.close();
	}

}
