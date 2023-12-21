package models.requestModels;
import models.AdditionalModels.Header;
import models.AdditionalModels.BodyRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnvelopeRequest {
    public Header Header;
    public BodyRequest BodyRequest;
}
