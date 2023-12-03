package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.nio.ByteBuffer;

import kotlin.UninitializedPropertyAccessException;

/**
 * The test suite for {@link ConfigGlAttribute} class.
 */
public final class ConfigGlAttributeTest {

    /**
     * Tests the default constructor of {@link ConfigGlAttribute}.
     */
    @Test
    public void testDefaultConfigGlAttribute() {
        Assertions.assertThatThrownBy(() -> ConfigGlAttribute.Builder.Companion.create().build())
            .as("The ConfigGlAttribute#Builder#build shouldn't be possible with the default values")
            .isInstanceOf(UninitializedPropertyAccessException.class);
    }

    /**
     * Tests the building of {@link ConfigGlAttribute} with valid values.
     */
    @Test
    public void testBuildConfigGlAttribute() {
        final ConfigGlAttribute.Builder builder = ConfigGlAttribute.Builder.Companion.create();
        final String attributeName = "test name";
        final ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        final int attributeLocation = 123;
        final int attributeComponentsInBuffer = 456;
        builder.setAttributeName(attributeName);
        builder.setBuffer(byteBuffer);
        builder.setAttributeLocation(attributeLocation);
        builder.setComponentsInBuffer(attributeComponentsInBuffer);
        final ConfigGlAttribute configGlAttribute = builder.build();

        Assertions.assertThat(configGlAttribute.getAttributeName())
            .as("AttributeName not the expected value.")
            .isEqualTo(attributeName);

        Assertions.assertThat(configGlAttribute.getBuffer())
            .as("Buffer not the expected value.")
            .isSameAs(byteBuffer);

        Assertions.assertThat(configGlAttribute.getAttributeLocation())
            .as("AttributeLocation not the expected value.")
            .isEqualTo(attributeLocation);

        Assertions.assertThat(configGlAttribute.getComponentsInBuffer())
            .as("ComponentsInBuffer not the expected value.")
            .isEqualTo(attributeComponentsInBuffer);
    }

    /**
     * Tests the {@link ConfigGlAttribute.Builder#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigGlAttributeBuilderToString() {
        final String configGlAttributeBuilderStr = ConfigGlAttribute.Builder.Companion.create().toString();
        Assertions.assertThat(configGlAttributeBuilderStr)
            .as("The toString of ConfigGlAttribute.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
