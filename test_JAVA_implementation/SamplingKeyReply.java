import java.io.Serializable;
import java.util.List;

public class SamplingKeyReply implements Serializable {
    private List<String> samplingKeys;

    public SamplingKeyReply(List<String> samplingKeys) {
        this.samplingKeys = samplingKeys;
    }

    public List<String> getSamplingKeys() {
        return samplingKeys;
    }
}
