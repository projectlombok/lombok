import lombok.Cleanup;
import java.io.*;
class CleanupPlain {
  CleanupPlain() {
    super();
  }
  void test() throws Exception {
    @lombok.Cleanup InputStream in = new FileInputStream("in");
    try 
      {
        @Cleanup OutputStream out = new FileOutputStream("out");
        try 
          {
            if (in.markSupported())
                {
                  out.flush();
                }
          }
        finally
          {
            out.close();
          }
      }
    finally
      {
        in.close();
      }
  }
}