package flinkdemo;

public final class SpedCalculator {

	private static double kmph_to_mps(double kmph) {
	      return 0.277778 * kmph;
	}
	private static double mps_to_kmph(double mps) {
	      return 3.6 * mps;
	}  

	public static double speedmps(double milliseconds, double totalmeters) {
	      return totalmeters / (milliseconds * 1000);
	}

	public static double speedkmph(double milliseconds, double totalmeters) {
	      return mps_to_kmph(speedmps(milliseconds, totalmeters));
	}
	
}
