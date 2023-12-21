package models.responseModels;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.AdditionalModels.Return;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    @JsonProperty("return")
    public Return myreturn;
}