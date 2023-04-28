/**
 * The package of Android instrumentation tests for functionality of the whole Ray Tracing engine.
 * <p>
 * Since there is a limitation where the code coverage of Jacoco does not include the native code,
 * then it was implemented a workaround where these tests are duplicate as native unit tests.
 * By doing this, these Android tests validate the output and the equivalent native unit tests just
 * make sure to exercise the whole Ray Tracing engine so the code coverage of native code via LCOV
 * is as accurate as possible.
 * <p>
 * So, each of these test class has an equivalent C++ class for the same tests using native
 * code coverage tool (LCOV).
 * E.g.:
 * AcceleratorTest       -> Android tests which validates Bitmap output.
 *                          These tests are placed in this folder.
 * AcceleratorEngineTest -> Native C++ tests which do the same as 'AcceleratorTest' and updates
 *                          the code coverage so it's more accurate.
 *                          These tests are placed in: 'app/Unit_Testing/engine/'
 */
package puscas.mobilertapp.engine;
