package uk.org.webcompere.testgadgets.testdataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;



/**
 * Implements loading using Jackson
 */
public class JsonLoader implements ObjectLoader {
    private ObjectMapper objectMapper;

    /**
     * Default constructor uses default object mapper
     */
    public JsonLoader() {
        this(new ObjectMapper());
    }

    /**
     * Construct with your own object mapper
     * @param objectMapper object mapper
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public JsonLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object load(Path source, Type targetType) throws IOException {
        return objectMapper.readValue(source.toFile(), objectMapper.constructType(targetType));
    }
}
