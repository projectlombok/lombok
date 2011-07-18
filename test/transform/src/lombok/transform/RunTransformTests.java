package lombok.transform;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestLombokFilesIdempotent.class, TestSourceFiles.class, TestWithDelombok.class, TestWithEcj.class, TestWithEclipse.class})
public class RunTransformTests {
}
