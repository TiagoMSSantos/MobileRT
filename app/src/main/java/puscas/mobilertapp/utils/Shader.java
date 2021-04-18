package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java8.util.J8Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 * The available shaders for the Ray Tracer engine.
 */
@RequiredArgsConstructor
@Log
public enum Shader {

    /**
     * The NoShadows shader.
     */
    NO_SHADOWS("NoShadows"),

    /**
     * The Whitted shader.
     */
    WHITTED("Whitted"),

    /**
     * The Path Tracing shader.
     */
    PATH_TRACING("PathTracing"),

    /**
     * The DepthMap shader.
     */
    DEPTH_MAP("DepthMap"),

    /**
     * The Diffuse shader.
     */
    DIFFUSE("Diffuse");

    /**
     * The name of the shader for the Ray Tracer engine.
     */
    @Getter
    private final String name;

    /**
     * Gets the names of all available shaders.
     */
    @NonNull
    public static String[] getNames() {
        log.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Shader::getName)
            .toArray(String[]::new);
    }
}
