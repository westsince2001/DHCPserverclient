public class Experiment2 {
  public static void main(String[] argv) throws Exception {
    long start = System.currentTimeMillis();

    Thread.sleep(2100);

    // Get elapsed time in milliseconds
    long elapsedTimeMillis = System.currentTimeMillis() - start;
    // 
    System.out.println(System.currentTimeMillis());
    float elapsedTimeSec = elapsedTimeMillis/1000F;
    System.out.println(elapsedTimeSec);
    
  }
}