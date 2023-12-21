import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.response.Response;
import models.AdditionalModels.AdditionalParameters;
import models.AdditionalModels.fullPd;
import models.responseModels.CustomerResponse;
import models.requestModels.*;
import models.responseModels.*;
import org.testng.annotations.Test;


public class ExampleUser extends BaseTest {

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

    @Test(description = "Получение токена",enabled = false)
    public void getTokenByAdmin(){

        LoginRequest loginRequest = new LoginRequest("user","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        System.out.println(token.getToken());
    }
    @Test(description = "Получение свободных номеров",enabled = false)
    public void getEmptyPhoneByAdmin(){
        LoginRequest loginRequest = new LoginRequest("user","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        PhonesResponse phoneNums = ADMIN_STEPS.getEmptyPhones(token.getToken());
        System.out.println(phoneNums);
    }
    @Test (description = "Создание нового кастомера",enabled = false)
    public void postCustomerByAdmin(){
        LoginRequest loginRequest = new LoginRequest("user","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, string);
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        System.out.println(id.getId());
    }
    @Test (description = "Получение данных кастомера по id",enabled = false)
    public void getCustomerByIdByAdmin() throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest("user","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, string);
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        CustomerResponse cResp = ADMIN_STEPS.getCustomer(token.getToken(),id.getId());
        System.out.println(cResp);
        System.out.println(cResp.getMyreturn().getPd().getPassportNumber());
        String json = cResp.getMyreturn().getPd().getPassportNumber();
        fullPd fullPd = new ObjectMapper().readValue(json,fullPd.class);
        System.out.println(fullPd.getPassportNumber()+"\n");
        System.out.println(fullPd.getPassportSeries()+"\n");
    }
    @Test (description = "Поиск кастомер Id по номеру XML",enabled = false)
    public void postXMLCustomerFindByPhoneNumber() throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest("user","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        String requestXML = getString(token,79285012886L);
        Response response = ADMIN_STEPS.getXMLId(requestXML);
        EnvelopeResponse eResp = new XmlMapper().readValue(response.getBody().asString(),EnvelopeResponse.class);
        System.out.println(eResp.getBodyResponse());
    }

    @Test (description = "Смена статуса пользователя админом",enabled = false)
    public void postChangeStatusByAdmin(){
        LoginRequest loginRequest = new LoginRequest("user","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, new AdditionalParameters("string"));
        StatusRequest statusRequest = new StatusRequest("Advanced");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        ADMIN_STEPS.changeStatus(token.getToken(),id.getId(),statusRequest,401);
        System.out.println();
    }

    @Test(description = "Тест Бизнес-сценария «Активация абонента» пользователем user",
            retryAnalyzer = Retry.class)
    public void bigTest() throws InterruptedException, JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        StatusRequest statusRequest = new StatusRequest("Advanced");
        TokenResponse token = USER_STEPS.postLogin(loginRequest); // Шаг 1
        PhonesResponse phoneNums = USER_STEPS.getEmptyPhones(token.getToken()); // Шаг 2
        if (phoneNums.getPhones()==null){
            System.out.println("Свободных номеров нет\n");
        } else{
            System.out.println("Свободный список номеров\n"+phoneNums);
        }

        PhoneResponse firstPhoneResponse = phoneNums.getPhones()[1];
        Long firstPhoneNumber = firstPhoneResponse.getPhone();
        CustomerRequest cReq = new CustomerRequest("Миша", firstPhoneNumber, new AdditionalParameters("М"));
        IdResponse id = USER_STEPS.postCustomer(cReq,token.getToken()); // Шаг 3

        Thread.sleep(200); // Шаг 4

        CustomerResponse cResp = USER_STEPS.getCustomer(token.getToken(),id.getId()); // Шаг 5
        System.out.println(cResp+"\n");
        String json = cResp.getMyreturn().getPd().getPassportNumber();
        fullPd fullPd = new ObjectMapper().readValue(json,fullPd.class);
        USER_STEPS.checkEqualsResponse("NEW",cResp.getMyreturn().getStatus());
        USER_STEPS.checkEqualsResponse(firstPhoneNumber,cResp.getMyreturn().getPhone());
        USER_STEPS.checkEqualsResponse(cReq.getName(),cResp.getMyreturn().getName());
        USER_STEPS.checkEqualsResponse(cReq.getAdditionalParameters(),cResp.getMyreturn().getAdditionalParameters());
        USER_STEPS.checkEqualsResponse(6,fullPd.getPassportNumber().length());
        USER_STEPS.checkEqualsResponse(4,fullPd.getPassportSeries().length());
        USER_STEPS.checkEqualsResponse(cReq.getAdditionalParameters(),cResp.getMyreturn().getAdditionalParameters());

        String requestXML = getString(token,firstPhoneNumber); // Шаг 6
        Response response = USER_STEPS.getXMLId(requestXML);
        EnvelopeResponse eResp = new XmlMapper().readValue(response.getBody().asString(),EnvelopeResponse.class);
        USER_STEPS.checkEqualsResponse(id.getId(),eResp.getBodyResponse().getCustomerId());

        USER_STEPS.changeStatus(token.getToken(),id.getId(),statusRequest,401);
        CustomerResponse changedCResp = USER_STEPS.getCustomer(token.getToken(),id.getId());
        USER_STEPS.checkEqualsResponse("NEW",changedCResp.getMyreturn().getStatus());
    }
}
