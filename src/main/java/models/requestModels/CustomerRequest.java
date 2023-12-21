package models.requestModels;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.AdditionalModels.AdditionalParameters;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {
    private String name;
    private Long phone;
    @JsonProperty("additionalParameters")
    AdditionalParameters additionalParameters;

}