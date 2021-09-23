package sample;

import java.io.*;
import java.util.stream.Collectors;

public class TextFileReader {
	public String read(File file) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		return br.lines().collect(Collectors.joining(System.lineSeparator()));
	}
}