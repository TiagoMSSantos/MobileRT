package puscas.mobilertapp.web.pojos;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instance_id",
    "base_uri"
})

/**
 * The POJO for the response of Create Consumer in Kafka.
 */
public class ResponseCreateConsumer {

    @JsonProperty("instance_id")
    private String instanceId = null;

    @JsonProperty("base_uri")
    private String baseUri = null;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @NonNull @JsonProperty("instance_id")
    public final String getInstanceId() {
        return this.instanceId;
    }

    @JsonProperty("instance_id")
    public final void setInstanceId(@NonNull final String instanceId) {
        this.instanceId = instanceId;
    }

    @NonNull @JsonProperty("base_uri")
    public final String getBaseUri() {
        return this.baseUri;
    }

    @JsonProperty("base_uri")
    public final void setBaseUri(@NonNull final String baseUri) {
        this.baseUri = baseUri;
    }

    @NonNull @JsonAnyGetter
    public final Map<String, Object> getAdditionalProperties() {
        return Collections.unmodifiableMap(this.additionalProperties);
    }

    @JsonAnySetter
    public final void setAdditionalProperty(@NonNull final String name, @NonNull final Object value) {
        this.additionalProperties.put(name, value);
    }

}
