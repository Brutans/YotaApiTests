import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.response.Response;
import models.AdditionalModels.AdditionalParameters;
import models.responseModels.CustomerResponse;
import models.requestModels.*;
import models.responseModels.*;
import org.apache.http.HttpException;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.time.Duration;


public class Example extends BaseTest {

    private static String getString(TokenResponse token) {
        long phoneNum = 79285012886L;
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

    public class RetryAnalyzer implements IRetryAnalyzer {

        private int count = 0;
        @Override public boolean retry(ITestResult iTestResult) {
            if (!iTestResult.isSuccess()) {
                int maxTry = 25;
                if (count < maxTry) {
                    count++;
                    iTestResult.setStatus(ITestResult.FAILURE);

                    iTestResult.getTestContext().getFailedTests().removeResult(iTestResult);

                    return true;
                } else {
                    iTestResult
                            .setStatus(ITestResult.FAILURE);
                }
            } else {
                iTestResult
                        .setStatus(ITestResult.SUCCESS);
            }
            return false;
        }
    }


    @Test(description = "Получение токена")
    public void getTokenByAdmin(){

        LoginRequest loginRequest = new LoginRequest("admin","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        System.out.println(token.getToken());
    }
    @Test(description = "Получение свободных номеров")
    public void getEmptyPhoneByAdmin(){
        LoginRequest loginRequest = new LoginRequest("admin","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        PhonesResponse phoneNums = ADMIN_STEPS.getEmptyPhones(token.getToken());
        System.out.println(phoneNums);
    }
    @Test (description = "Создание нового кастомера")
    public void postCustomerByAdmin(){
        LoginRequest loginRequest = new LoginRequest("admin","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, string);
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        System.out.println(id.getId());
    }
    @Test (description = "Получение данных кастомера по id")
    public void getCustomerByIdByAdmin(){
        LoginRequest loginRequest = new LoginRequest("admin","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, string);
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        CustomerResponse CResp = ADMIN_STEPS.getCustomer(token.getToken(),id.getId());
        System.out.println(CResp);
    }
    @Test (description = "Поиск кастомер Id по номеру XML")
    public void postXMLCustomerFindByPhoneNumber() throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest("admin","password");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        String requestXML = getString(token);
        Response response = ADMIN_STEPS.getXMLId(requestXML);
        EnvelopeResponse eResp = new XmlMapper().readValue(response.getBody().asString(),EnvelopeResponse.class);
        System.out.println(eResp.getBodyResponse());
    }

    @Test (description = "Смена статуса пользователя админом")
    public void postChangeStatusByAdmin(){
        LoginRequest loginRequest = new LoginRequest("admin","password");
        AdditionalParameters string = new AdditionalParameters("string");
        CustomerRequest cReq = new CustomerRequest("name",79285012886L, new AdditionalParameters("string"));
        StatusRequest statusRequest = new StatusRequest("Advanced");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest);
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken());
        ADMIN_STEPS.changeStatus(token.getToken(),id.getId(),statusRequest);
        System.out.println();
    }

    @Test(description = "Тест Бизнес-сценария «Активация абонента»",retryAnalyzer = Retry.class)
    public void bigTest() throws InterruptedException {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        StatusRequest statusRequest = new StatusRequest("Advanced");
        TokenResponse token = ADMIN_STEPS.postLogin(loginRequest); // Шаг 1
        PhonesResponse phoneNums = ADMIN_STEPS.getEmptyPhones(token.getToken()); // Шаг 2
        if (phoneNums.getPhones()==null){
            System.out.println("Свободных номеров нет\n");
        } else{
            System.out.println("Свободный список номеров\n"+phoneNums);
        }
        //CustomerRequest cReq = new CustomerRequest("Миша", 79285012886L, new AdditionalParameters("М"));
        PhoneResponse firstPhoneResponse = phoneNums.getPhones()[1];
        Long firstPhoneNumber = firstPhoneResponse.getPhone();
        CustomerRequest cReq = new CustomerRequest("Миша", firstPhoneNumber, new AdditionalParameters("М"));
        IdResponse id = ADMIN_STEPS.postCustomer(cReq,token.getToken()); // Шаг 3

        Thread.sleep(100); // Шаг 4

        CustomerResponse cResp = ADMIN_STEPS.getCustomer(token.getToken(),id.getId());
        System.out.println(firstPhoneNumber+"\n");
        System.out.println(cResp.getMyreturn().getPhone()+"\n");
        System.out.println(cResp+"\n");
        System.out.println(cResp.getMyreturn().getStatus()+"\n");
        System.out.println("NEW My");
        ADMIN_STEPS.checkEqualsResponse("NEW",cResp.getMyreturn().getStatus());
        ADMIN_STEPS.checkEqualsResponse(firstPhoneNumber,cResp.getMyreturn().getPhone());

    }
}
