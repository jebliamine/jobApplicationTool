package de.jeb.japp.generation.service.provider;

import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CvProfileResponseParserTest {

    @Test
    void parsesWellFormedJson() {
        String json = """
                {"fullName":"Jane Doe","summary":"A summary.","experiences":[
                    {"company":"Acme","title":"Engineer","startDate":"2020-01-01","endDate":null,"description":"Built things."}
                ]}""";

        CvProfileExtractionResult result = CvProfileResponseParser.parse(json);

        assertThat(result.fullName()).isEqualTo("Jane Doe");
        assertThat(result.summary()).isEqualTo("A summary.");
        assertThat(result.experiences()).hasSize(1);
        assertThat(result.experiences().get(0).company()).isEqualTo("Acme");
        assertThat(result.experiences().get(0).endDate()).isNull();
    }

    @Test
    void stripsAMarkdownCodeFenceAroundTheJson() {
        String fenced = "```json\n{\"fullName\":null,\"summary\":null,\"experiences\":[]}\n```";

        CvProfileExtractionResult result = CvProfileResponseParser.parse(fenced);

        assertThat(result.fullName()).isNull();
        assertThat(result.experiences()).isEmpty();
    }

    @Test
    void throwsACvProfileGenerationExceptionOnInvalidJson() {
        assertThatThrownBy(() -> CvProfileResponseParser.parse("not json at all"))
                .isInstanceOf(CvProfileGenerationException.class);
    }
}
