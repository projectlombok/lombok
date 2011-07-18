package lombok;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({lombok.transform.RunTransformTests.class, lombok.bytecode.RunBytecodeTests.class})
public class RunAllTests {
}
