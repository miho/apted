/* MIT License
 *
 * Copyright (c) 2017 Database Research Group Salzburg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Collection;
import java.util.Arrays;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.google.gson.Gson;
import static org.junit.Assert.assertEquals;
import distance.APTED;
import util.LblTree;
import parser.BracketStringInputParser;
import node.Node;
import node.StringNodeData;
import costmodel.StringUnitCostModel;

/**
 * Correctness unit tests of distance and mapping computation.
 *
 * <p>In case of mapping, only mapping cost is verified against the correct
 * distance.
 *
 * <p>Currently tests only for unit-cost model and single string-value labels.
 *
 * @see StringNodeData
 * @see StringUnitCostModel
 */
@RunWith(Parameterized.class)
public class CorrectnessTest {

  /**
   * Path to JSON file with test cases. Currently only unit cost for single
   * string-value labels.
   */
  private static final String CORRECTNESS_TESTS_PATH = "correctness_test_cases.json";

  /**
   * APTED algorithm initialized once for each test case. Currently only
   * unit-cost test cases are implemented.
   */
  private APTED apted = new APTED((float)1.0, (float)1.0, (float)1.0);

  /**
   * Test case object holding parameters of a single test case.
   *
   * <p>Could be also deserialized here but without much benefit.
   */
  private TestCase t;

  /**
   * This class represents a single test case from the JSON file. JSON keys
   * are mapped to fiels of this class.
   *
   * <p><b>[TODO]</b> Verify if this is the best placement for this class.
   */
  private static class TestCase {

    /**
     * Test identifier to quickly find failed test case in JSON file.
     */
    private int testID;

    /**
     * Source tree as string.
     */
    private String t1;

    /**
     * Destination tree as string.
     */
    private String t2;

    /**
     * Correct distance value between source and destination trees.
     */
    private int d;

    /**
     * Used in printing the test case details on failure with '(name = "{0}")'.
     *
     * @return test case details.
     * @see CorrectnessTest#data()
     */
    public String toString() {
      return "testID:" + testID + ",t1:" + t1 + ",t2:" + t2 + ",d:" + d;
    }

    /**
     * Returns identifier of this test case.
     *
     * @return test case identifier.
     */
    public int getTestID() {
      return testID;
    }

    /**
     * Returns source tree of this test case.
     *
     * @return source tree.
     */
    public String getT1() {
      return t1;
    }

    /**
     * Returns destination tree of this test case.
     *
     * @return destination tree.
     */
    public String getT2() {
      return t2;
    }

    /**
     * Returns correct distance value between source and destination trees
     * of this test case.
     *
     * @return correct distance.
     */
    public int getD() {
      return d;
    }

  }

  /**
   * Constructs a single test for a single test case. Used for parameterised
   * tests.
   *
   * @param t single test case.
   */
  public CorrectnessTest(TestCase t) {
    this.t = t;
  }

  /**
   * Returns a list of test cases read from external JSON file.
   *
   * <p>Uses google.gson for reading JSON document.
   *
   * <p>In case of a failure, the parameter values from {@link TestCase} object
   * are printed '(name = "{0}")'.
   *
   * @return list of all test cases read from JSON file.
   * @throws IOException in case of failure of reading the JSON file.
   */
  @Parameters(name = "{0}")
  public static Collection data() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(CorrectnessTest.class.getResource("/"+CORRECTNESS_TESTS_PATH).getPath()));
    Gson gson = new Gson();
    TestCase[] testCases = new Gson().fromJson(br, TestCase[].class);
    return Arrays.asList(testCases);
  }

  /**
   * Compute TED for a single test case and compare to the correct value. Uses
   * node labels with a single string value and unit cost model.
   */
  @Test
  public void correctnessDistanceTestStringUnitCost() {
    BracketStringInputParser parser = new BracketStringInputParser();
    Node<StringNodeData> t1 = parser.fromString(t.getT1());
    Node<StringNodeData> t2 = parser.fromString(t.getT1());
    // This cast is safe due to unit cost.
    int result = (int)apted.nonNormalizedTreeDist(t1, t2);
    assertEquals(t.getD(), result);
  }

  /**
   * Compute TED for a single test case and compare to the correct value.
   */
  // @Test
  public void correctDistanceTest() {
    LblTree t1 = LblTree.fromString(this.t.getT1());
    LblTree t2 = LblTree.fromString(this.t.getT2());
    // This cast is safe due to unit cost.
    int result = (int)apted.nonNormalizedTreeDist(t1, t2);
    assertEquals(this.t.getD(), result);
  }

  /**
   * Compute TED for swapped input trees from a single test case and compare
   * to the correct value.
   */
  // @Test
  public void correctDistanceTestSymmetric() {
    LblTree t1 = LblTree.fromString(this.t.getT1());
    LblTree t2 = LblTree.fromString(this.t.getT2());
    // This cast is safe due to unit cost.
    int result = (int)apted.nonNormalizedTreeDist(t2, t1);
    assertEquals(this.t.getD(), result);
  }

  /**
   * Compute minimum-cost edit mapping for a single test case and compare its
   * cost to the correct TED value.
   */
  // @Test
  public void correctMappingCostTest() {
    LblTree t1 = LblTree.fromString(this.t.getT1());
    LblTree t2 = LblTree.fromString(this.t.getT2());
    // TED must be computed before the mapping.
    // This cast is safe due to unit cost.
    int result = (int)apted.nonNormalizedTreeDist(t1, t2);
    LinkedList<int[]> mapping = apted.computeEditMapping();
    // This cast is safe due to unit cost.
    result = (int)apted.mappingCost(mapping);
    assertEquals(this.t.getD(), result);
  }

}
