
package de.uhh.l2g.webservices.videoprocessor.model.opencast;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Auto-generated by http://www.jsonschema2pojo.org/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "video-1",
    "audio-1"
})
public class Streams {

    @JsonProperty("video-1")
    private Video1 video1;
    @JsonProperty("audio-1")
    private Audio1 audio1;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("video-1")
    public Video1 getVideo1() {
        return video1;
    }

    @JsonProperty("video-1")
    public void setVideo1(Video1 video1) {
        this.video1 = video1;
    }

    @JsonProperty("audio-1")
    public Audio1 getAudio1() {
        return audio1;
    }

    @JsonProperty("audio-1")
    public void setAudio1(Audio1 audio1) {
        this.audio1 = audio1;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
