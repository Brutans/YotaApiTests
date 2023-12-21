package models.responseModels;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import models.AdditionalModels.BodyResponse;
import models.AdditionalModels.Header;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnvelopeResponse {
    @JacksonXmlProperty(localName = "Header")
    public Header header;
    @JacksonXmlProperty(localName = "Body")
    public BodyResponse bodyResponse;
}
