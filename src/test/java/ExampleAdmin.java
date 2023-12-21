import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.AdditionalModels.AdditionalParameters;
import models.AdditionalModels.fullPd;
import models.responseModels.CustomerResponse;
import models.requestModels.*;
import models.responseModels.*;
import org.testng.annotations.Test;


@Feature("Your Feature")
@Story("Your Story")
public class ExampleAdmin extends BaseTest {

    private static String getString(TokenResponse token, Long phoneNum) {

        return ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns3:Envelope xmlns:ns2=\"soap\" xmlns:ns3=\"http://schemas.xmlsoap.org/soap/envelope\">\n" +
                "    <ns2:Header>\n" +
                "        <authToken>"+ token.getToken()+"</authToken>\n" +
                "    </ns2:Header>\n" +
                "    <ns2:Body>\n" +
                "        <phoneNumber>"+phoneNum+"</phoneNumber>\n" +
                "    </ns2:Body>\n" +
                "</ns3:Envelope>\n");
    }
    @Issue("Проверка бизнес-сценария")
    @Test(description = "Тест Бизнес-сценария «Активация абонента» пользователем admin",
            retryAnalyzer = Retry.class)
    public void bigTestByAdmin() throws InterruptedException, JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest("admin", "password");
        StatusRequest statusRequest = new StatusRequest("Advanced");

        Allure.step("Step 1: Пользователь авторизируется под своим логином ");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest); // Шаг 1

        Allure.step("Step 2: Пользователь получает список свободных номеров");
        PhonesResponse phoneNums = ADMIN_STEPS.getEmptyPhones(token.getToken()); // Шаг 2
        if (phoneNums.getPhones()==null){
            System.out.println("Свободных номеров нет\n");
        } else{
            System.out.println("Свободный список номеров\n"+phoneNums);
        }

        Allure.step("Step 3: Пользователь создаёт нового кастомера");
        PhoneResponse firstPhoneResponse = phoneNums.getPhones()[1]; // Шаг 3
        Long firstPhoneNumber = firstPhoneResponse.getPhone();
        CustomerRequest cReq = new CustomerRequest("Миша", firstPhoneNumber, new AdditionalParameters("М"));
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());

        Allure.step("Step 4: Ожидание активации кастомера");
        Thread.sleep(120000); // Шаг 4

        Allure.step("Step 5: Проверка корректности активации кастомера");
        CustomerResponse cResp = ADMIN_STEPS.getCustomer(token.getToken(),id.getId()); // Шаг 5
        System.out.println(cResp+"\n");
        String json = cResp.getMyreturn().getPd().getPassportNumber();
        fullPd fullPd = new ObjectMapper().readValue(json,fullPd.class);

        ADMIN_STEPS.checkEqualsResponse("ACTIVE",cResp.getMyreturn().getStatus());
        ADMIN_STEPS.checkEqualsResponse(firstPhoneNumber,cResp.getMyreturn().getPhone());
        ADMIN_STEPS.checkEqualsResponse(cReq.getName(),cResp.getMyreturn().getName());
        ADMIN_STEPS.checkEqualsResponse(cReq.getAdditionalParameters(),cResp.getMyreturn().getAdditionalParameters());
        ADMIN_STEPS.checkEqualsResponse(6,fullPd.getPassportNumber().length());
        ADMIN_STEPS.checkEqualsResponse(4,fullPd.getPassportSeries().length());
        ADMIN_STEPS.checkEqualsResponse(cReq.getAdditionalParameters(),cResp.getMyreturn().getAdditionalParameters());

        Allure.step("Step 6: Пользователь проверяет, что кастомер сохранился в старой системе");
        String requestXML = getString(token,firstPhoneNumber); // Шаг 6
        Response response = ADMIN_STEPS.getXMLId(requestXML);
        EnvelopeResponse eResp = new XmlMapper().readValue(response.getBody().asString(),EnvelopeResponse.class);
        ADMIN_STEPS.checkEqualsResponse(id.getId(),eResp.getBodyResponse().getCustomerId());

        Allure.step("Step 7: Пользователь пытается изменить кастомеру статус");
        ADMIN_STEPS.changeStatus(token.getToken(),id.getId(),statusRequest,200); // Шаг 7
        CustomerResponse changedCResp = ADMIN_STEPS.getCustomer(token.getToken(),id.getId());

        Allure.step("Step 8: Пользователь проверяет, что изменение статуса прошло успешно");
        ADMIN_STEPS.checkEqualsResponse("Advanced",changedCResp.getMyreturn().getStatus()); // Шаг 8

    }
}
