package OldFiles;
import java.util.Arrays;


public class ExperimentPrintBytes {
	public static void main(String[] args) {
		byte byteArray = (byte)5;
		System.out.println(Integer.toBinaryString((byteArray + 256)%256)+Integer.toBinaryString((byteArray + 256)%256));

	}
}
