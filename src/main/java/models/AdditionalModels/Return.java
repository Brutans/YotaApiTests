package models.AdditionalModels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Return {
    private String customerId;
    private String name;
    private String status;
    private Long phone;
    private AdditionalParameters additionalParameters;
    private pd pd;
}

