import java.io.Serializable;
import java.util.List;

public class SamplingKeyReply implements Serializable {
    private String workerIP;
    private List<String> samplingKeys;

    public SamplingKeyReply(String workerIP, List<String> samplingKeys) {
        this.workerIP = workerIP;
        this.samplingKeys = samplingKeys;
    }

    public String getWorkerIP() {
        return workerIP;
    }

    public List<String> getSamplingKeys() {
        return samplingKeys;
    }
}
