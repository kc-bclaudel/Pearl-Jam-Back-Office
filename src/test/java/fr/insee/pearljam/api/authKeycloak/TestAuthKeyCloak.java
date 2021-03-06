package fr.insee.pearljam.api.authKeycloak;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import fr.insee.pearljam.api.controller.WsText;
import fr.insee.pearljam.api.domain.Campaign;
import fr.insee.pearljam.api.domain.CommentType;
import fr.insee.pearljam.api.domain.ContactOutcomeType;
import fr.insee.pearljam.api.domain.Message;
import fr.insee.pearljam.api.domain.MessageStatusType;
import fr.insee.pearljam.api.domain.StateType;
import fr.insee.pearljam.api.domain.Status;
import fr.insee.pearljam.api.domain.Visibility;
import fr.insee.pearljam.api.dto.comment.CommentDto;
import fr.insee.pearljam.api.dto.contactattempt.ContactAttemptDto;
import fr.insee.pearljam.api.dto.contactoutcome.ContactOutcomeDto;
import fr.insee.pearljam.api.dto.message.MessageDto;
import fr.insee.pearljam.api.dto.state.StateDto;
import fr.insee.pearljam.api.dto.surveyunit.SurveyUnitDetailDto;
import fr.insee.pearljam.api.repository.CampaignRepository;
import fr.insee.pearljam.api.repository.MessageRepository;
import fr.insee.pearljam.api.repository.SurveyUnitRepository;
import fr.insee.pearljam.api.repository.VisibilityRepository;
import fr.insee.pearljam.api.service.SurveyUnitService;
import fr.insee.pearljam.api.service.UserService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/* Test class for Keycloak Authentication */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "test" })
@ContextConfiguration(initializers = { TestAuthKeyCloak.Initializer.class })
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties= {"fr.insee.pearljam.application.mode = KeyCloak"})
public class TestAuthKeyCloak {
	
	@Autowired
	SurveyUnitService surveyUnitService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	SurveyUnitRepository surveyUnitRepository;
	
	@Autowired
	CampaignRepository campaignRepository;
  
	@Autowired
	VisibilityRepository visibilityRepository;
	
	@Autowired
	MessageRepository messageRepository;
	
	@Container
	public static KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("realm.json");
	
	@LocalServerPort
	int port;

	public Liquibase liquibase;
	
	public static final String CLIENT_SECRET = "8951f422-44dd-45b4-a6ac-dde6748075d7";
	public static final String CLIENT = "client-web";
	
	/**
	 * This method set up the port of the PostgreSqlContainer
	 * @throws SQLException
	 * @throws LiquibaseException
	 */
	@BeforeEach
	public void setUp() throws SQLException, LiquibaseException {
		RestAssured.port = port;
	}

	/**
	 * Defines the configuration of the PostgreSqlContainer
	 */
	@SuppressWarnings("rawtypes")
	@Container
	@ClassRule
	public static PostgreSQLContainer postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer("postgres")
			.withDatabaseName("pearljam").withUsername("pearljam").withPassword("pearljam");

	
	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues
					.of("spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
							"spring.datasource.username=" + postgreSQLContainer.getUsername(),
							"spring.datasource.password=" + postgreSQLContainer.getPassword(),
							"keycloak.auth-server-url=" + keycloak.getAuthServerUrl())
					.applyTo(configurableApplicationContext.getEnvironment());
		}
	}
	
	/**
	 * This method is use to check if the dates are correct
	 * @param dateType
	 * @param date
	 * @return
	 */
	private boolean testingDates(String dateType, long date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		LocalDate localDateNow = LocalDate.now();
		boolean check = false;
		LocalDate value = LocalDate.parse(df.format(date));
		switch(dateType) {
			case ("managementStartDate") :
				if(value.equals(localDateNow.minusDays(4))) {
					check = true;
				}
				break;
			case ("interviewerStartDate") :
				if(value.equals(localDateNow.minusDays(3))) {
					check = true;
				}
				break;
			case ("identificationPhaseStartDate") :
				if(value.equals(localDateNow.minusDays(2))) {
					check = true;
				}
				break;
			case ("collectionStartDate") :
				if(value.equals(localDateNow.plusDays(2))) {
					check = true;
				}
				break;
			case ("collectionEndDate") :
				if(value.equals(localDateNow.plusMonths(1))) {
					check = true;
				}
				break;
			case ("endDate") :
				if(value.equals(localDateNow.plusMonths(2))) {
					check = true;
				}
				break;
			default:
				return check;
		}
		return check;
	}
	/***
	 * This method retreive the access token of the keycloak client
	 * @param clientId
	 * @param clientSecret
	 * @param username
	 * @param password
	 * @return
	 * @throws JSONException
	 */
	public String resourceOwnerLogin(String clientId, String clientSecret, String username, String password) throws JSONException {
	      Response response =
	              given().auth().preemptive().basic(clientId, clientSecret)   
	                      .formParam("grant_type", "password")
	                      .formParam("username", username)
	                      .formParam("password", password)
	                      .when()
	                      .post( keycloak.getAuthServerUrl() + "/realms/insee-realm/protocol/openid-connect/token");
	      JSONObject jsonObject = new JSONObject(response.getBody().asString());
	      String accessToken = jsonObject.get("access_token").toString();
	      return accessToken;
	   }

	/*UserController*/
	
	/**
	 * Test that the GET endpoint "api/user"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(1)
	public void testGetUser() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/user").then().statusCode(200).and()
		.assertThat().body("id", equalTo("ABC")).and()
		.assertThat().body("firstName", equalTo("Melinda")).and()
		.assertThat().body("lastName", equalTo("Webb")).and()
		.assertThat().body("organizationUnit.id", equalTo("OU-NORTH")).and()
		.assertThat().body("organizationUnit.label", equalTo("North region organizational unit")).and()
		.assertThat().body("localOrganizationUnits[0].id", equalTo("OU-NORTH")).and()
		.assertThat().body("localOrganizationUnits[0].label", equalTo("North region organizational unit"));
	}
	
	/**
	 * Test that the GET endpoint "api/user"
	 * return null
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(2)
	public void testGetUserNotFound() throws InterruptedException {
		assertEquals(null, userService.getUser("test"));
	}
	
	
	/*CampaignController*/
	
	/**
	 * Test that the GET endpoint "api/campaigns"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(3)
	public void testGetCampaign() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaigns").then().statusCode(200).and()
		.assertThat().body("id", hasItem("simpsons2020x00")).and()
		.assertThat().body("label", hasItem("Survey on the Simpsons tv show 2020")).and()
		.assertThat().body("allocated",hasItem(4)).and()
		.assertThat().body("toAffect",hasItem(0)).and()
		.assertThat().body("toFollowUp",hasItem(0)).and()
		.assertThat().body("toReview",hasItem(0)).and()
		.assertThat().body("finalized",hasItem(0)).and()
		.assertThat().body("toProcessInterviewer",hasItem(0)).and()
		.assertThat().body("preference",hasItem(false));
		
		//Testing dates
		assertTrue(testingDates("managementStartDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("managementStartDate[0]")));
		assertTrue(testingDates("interviewerStartDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("interviewerStartDate[0]")));
		assertTrue(testingDates("identificationPhaseStartDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("identificationPhaseStartDate[0]")));
		assertTrue(testingDates("collectionStartDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("collectionStartDate[0]")));
		assertTrue(testingDates("collectionEndDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("collectionEndDate[0]")));
		assertTrue(testingDates("endDate", given().auth().oauth2(accessToken).when().get("api/campaigns").path("endDate[0]")));

	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/interviewers"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(4)
	public void testGetCampaignInterviewer() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x00/interviewers").then().statusCode(200).and()
		.assertThat().body("id", hasItem("INTW1")).and()
		.assertThat().body("interviewerFirstName",hasItem("Margie")).and()
		.assertThat().body("interviewerLastName", hasItem("Lucas")).and()
		.assertThat().body("surveyUnitCount",hasItem(2));
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/interviewers"
	 * return 404 when campaign Id is false
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(5)
	public void testGetCampaignInterviewerNotFound() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x000000/interviewers").then().statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/state-count"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(6)
	public void testGetCampaignStateCount() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x00/survey-units/state-count").then().statusCode(200).and()
		.assertThat().body("organizationUnits.idDem", hasItem("OU-NORTH")).and()
		.assertThat().body("organizationUnits[0].nvmCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].nnsCount",equalTo(0)).and()
    	.assertThat().body("organizationUnits[0].anvCount",equalTo(1)).and()
		.assertThat().body("organizationUnits[0].vinCount",equalTo(3)).and()
		.assertThat().body("organizationUnits[0].vicCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].prcCount", equalTo(0)).and()
		.assertThat().body("organizationUnits[0].aocCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].apsCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].insCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].wftCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].wfsCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].tbrCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].finCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].qnaCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].qnaFinCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].nvaCount",equalTo(0)).and()
		.assertThat().body("organizationUnits[0].total",equalTo(4));
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/state-count"
	 * return 404 when campaign Id is false
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(7)
	public void testGetCampaignStateCountNotFound() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/test/survey-units/state-count").then().statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/interviewer/{id}/state-count"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(8)
	public void testGetCampaignInterviewerStateCount() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x00/survey-units/interviewer/INTW1/state-count").then().statusCode(200).and()
		.assertThat().body("idDem", equalTo(null)).and()
		.assertThat().body("nvmCount",equalTo(0)).and()
		.assertThat().body("nnsCount",equalTo(0)).and()
    	.assertThat().body("anvCount",equalTo(1)).and()
		.assertThat().body("vinCount",equalTo(1)).and()
		.assertThat().body("vicCount",equalTo(0)).and()
		.assertThat().body("prcCount",equalTo(0)).and()
		.assertThat().body("aocCount",equalTo(0)).and()
		.assertThat().body("apsCount",equalTo(0)).and()
		.assertThat().body("insCount",equalTo(0)).and()
		.assertThat().body("wftCount",equalTo(0)).and()
		.assertThat().body("wfsCount",equalTo(0)).and()
		.assertThat().body("tbrCount",equalTo(0)).and()
		.assertThat().body("finCount",equalTo(0)).and()
		.assertThat().body("qnaCount",equalTo(0)).and()
		.assertThat().body("qnaFinCount",equalTo(0)).and()
		.assertThat().body("nvaCount",equalTo(0)).and()
		.assertThat().body("total",equalTo(2));
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/interviewer/{id}/state-count"
	 * return 404 when campaign Id is false
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(9)
	public void testGetCampaignInterviewerStateCountNotFoundCampaign() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x000000/survey-units/interviewer/INTW1/state-count").then().statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/interviewer/{id}/state-count"
	 * return 404 when interviewer Id is false
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(10)
	public void testGetCampaignInterviewerStateCountNotFoundIntw() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/campaign/simpsons2020x00/survey-units/interviewer/test/state-count").then().statusCode(404);
	}

	
	/*SurveyUnitController*/
		
	/**
	 * Test that the GET endpoint "api/survey-unit/{id}"
	 * return 200.
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(11)
	public void testGetSurveyUnitDetail() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		given().auth().oauth2(accessToken).when().get("api/survey-unit/11").then().statusCode(200).and()
		.assertThat().body("id", equalTo("11")).and()
		.assertThat().body("firstName", equalTo("Ted")).and()
		.assertThat().body("lastName", equalTo("Farmer")).and()
		.assertThat().body("priority", is(true)).and()
		.assertThat().body("phoneNumbers", hasItems("+3351231231230")).and()
		.assertThat().body("address.l1", equalTo("Ted Farmer")).and()
		.assertThat().body("address.l2", equalTo("")).and()
		.assertThat().body("address.l3", equalTo("")).and()
		.assertThat().body("address.l4", equalTo("1 rue de la gare")).and()
		.assertThat().body("address.l5", equalTo("")).and()
		.assertThat().body("address.l6", equalTo("29270 Carhaix")).and()
		.assertThat().body("address.l7", equalTo("France")).and()
		.assertThat().body("geographicalLocation.id", equalTo("29024")).and()
		.assertThat().body("geographicalLocation.label", equalTo("CARHAIX PLOUGUER")).and()
		.assertThat().body("campaign", equalTo("simpsons2020x00")).and()
		.assertThat().body("contactOutcome", nullValue()).and()
		.assertThat().body("comments", empty()).and()
		.assertThat().body("states[0].type", equalTo("NNS")).and()
		.assertThat().body("contactAttempts", empty());
		
	}
	
	/**
	 * Test that the GET endpoint "api/survey-unit/"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(12)
	public void testGetAllSurveyUnit() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		given().auth().oauth2(accessToken).when().get("api/survey-units").then().statusCode(200).and()
		.assertThat().body("id", hasItem("11")).and()
		.assertThat().body("campaign", hasItem("simpsons2020x00")).and()
		.assertThat().body("campaignLabel",  hasItem("Survey on the Simpsons tv show 2020"));
		
		//Testing dates
		assertTrue(testingDates("managementStartDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("managementStartDate[0]")));
		assertTrue(testingDates("interviewerStartDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("interviewerStartDate[0]")));
		assertTrue(testingDates("identificationPhaseStartDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("identificationPhaseStartDate[0]")));
		assertTrue(testingDates("collectionStartDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("collectionStartDate[0]")));
		assertTrue(testingDates("collectionEndDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("collectionEndDate[0]")));
		assertTrue(testingDates("endDate", given().auth().oauth2(accessToken).when().get("api/survey-units").path("endDate[0]")));

	}
	
	
	/**
	 * Test that the GET endpoint "api/survey-unit/{id}"
	 * return 404 when survey-unit is false
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(13)
	public void testGetSurveyUnitDetailNotFound() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		given().auth().oauth2(accessToken).when().get("api/survey-unit/123456789")
		.then()
		.statusCode(404);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 200
	 * @throws InterruptedException
	 */
	@Test
	@Order(14)
	public void testPutSurveyUnitDetail() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "11");
		surveyUnitDetailDto.setFirstName("test");
		surveyUnitDetailDto.setLastName("test");
		surveyUnitDetailDto.setPhoneNumbers(List.of("+33555555555","+33666666666"));
		surveyUnitDetailDto.getAddress().setL1("test");
		surveyUnitDetailDto.getAddress().setL2("test");
		surveyUnitDetailDto.getAddress().setL3("test");
		surveyUnitDetailDto.getAddress().setL4("test");
		surveyUnitDetailDto.getAddress().setL5("test");
		surveyUnitDetailDto.getAddress().setL6("test");
		surveyUnitDetailDto.getAddress().setL7("test");
		surveyUnitDetailDto.setComments(List.of(new CommentDto(CommentType.INTERVIEWER, "test"),new CommentDto(CommentType.MANAGEMENT, "")));
		surveyUnitDetailDto.setStates(List.of(new StateDto(1L, 1590504459838L, StateType.NNS)));
		surveyUnitDetailDto.setContactAttempts(List.of(new ContactAttemptDto(1589268626000L, Status.NOC), new ContactAttemptDto(1589268800000L, Status.INA)));
		surveyUnitDetailDto.setContactOutcome(new ContactOutcomeDto(1589268626000L, ContactOutcomeType.INI, 2));
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/11")
		.then()
			.statusCode(200);
		Response response = given().auth().oauth2(accessToken).when().get("api/survey-unit/11");
		response.then().statusCode(200).and()
		.assertThat().body("id", equalTo("11")).and()
		.assertThat().body("firstName", equalTo("test")).and()
		.assertThat().body("lastName", equalTo("test")).and()
		.assertThat().body("phoneNumbers", hasItems("+33555555555", "+33666666666")).and()
		.assertThat().body("address.l1", equalTo("test")).and()
		.assertThat().body("address.l2", equalTo("test")).and()
		.assertThat().body("address.l3", equalTo("test")).and()
		.assertThat().body("address.l4", equalTo("test")).and()
		.assertThat().body("address.l5", equalTo("test")).and()
		.assertThat().body("address.l6", equalTo("test")).and()
		.assertThat().body("address.l7", equalTo("test")).and()
		.assertThat().body("contactOutcome.type", equalTo(ContactOutcomeType.INI.toString())).and()
		.assertThat().body("contactOutcome.totalNumberOfContactAttempts", is(2)).and()
		.assertThat().body("comments[0].value", equalTo("test")).and()
		.assertThat().body("comments[0].type", equalTo(CommentType.INTERVIEWER.toString())).and()
		.assertThat().body("comments[1].value", blankOrNullString()).and()
		.assertThat().body("comments[1].type", equalTo(CommentType.MANAGEMENT.toString())).and()
		.assertThat().body("contactAttempts[0].status", equalTo(Status.NOC.toString())).and()
		.assertThat().body("contactAttempts[1].status", equalTo(Status.INA.toString()));
		//Tests with Junit for Long values
		assertEquals(Long.valueOf(1589268626000L), response.then().extract().jsonPath().getLong("contactOutcome.date"));
		assertEquals(Long.valueOf(1589268626000L), response.then().extract().jsonPath().getLong("contactAttempts[0].date"));
		assertEquals(Long.valueOf(1589268800000L), response.then().extract().jsonPath().getLong("contactAttempts[1].date"));

	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 404 with false survey-unit Id
 	 * @throws InterruptedException
	 */
	@Test
	@Order(15)
	public void testPutSurveyUnitDetailErrorOnIds() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "12");
		surveyUnitDetailDto.setStates(List.of(new StateDto(null, 1589268626L, StateType.AOC),new StateDto(null, 1589268800L, StateType.APS)));
		given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/test")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 404 with state null
 	 * @throws InterruptedException
	 */
	@Test
	@Order(16)
	public void testPutSurveyUnitDetailErrorOnStates() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "11");
		surveyUnitDetailDto.setStates(List.of());
		given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/11")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 404 with address null
 	 * @throws InterruptedException
	 */
	@Test
	@Order(17)
	public void testPutSurveyUnitDetailErrorOnAddress() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "11");
		surveyUnitDetailDto.setAddress(null);
		surveyUnitDetailDto.setStates(List.of(new StateDto(null, 1589268626L, StateType.AOC),new StateDto(null, 1589268800L, StateType.APS)));
		given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/11")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 404 with first name empty
 	 * @throws InterruptedException
	 */
	@Test
	@Order(18)
	public void testPutSurveyUnitDetailErrorOnFirstName() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "11");
		surveyUnitDetailDto.setFirstName("");
		surveyUnitDetailDto.setStates(List.of(new StateDto(null, 1589268626L, StateType.AOC),new StateDto(null, 1589268800L, StateType.APS)));
		given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/11")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 404 with last name empty
 	 * @throws InterruptedException
	 */
	@Test
	@Order(19)
	public void testPutSurveyUnitDetailErrorOnLastName() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		SurveyUnitDetailDto surveyUnitDetailDto = surveyUnitService.getSurveyUnitDetail("INTW1", "11");
		surveyUnitDetailDto.setLastName("");
		surveyUnitDetailDto.setStates(List.of(new StateDto(null, 1589268626L, StateType.AOC)));
		given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(surveyUnitDetailDto))
		.when()
			.put("api/survey-unit/11")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}/state/{state}"
	 * return 200
 	 * @throws InterruptedException
	 */
	@Test
	@Order(20)
	public void testPutSurveyUnitState() throws InterruptedException, JSONException, JsonProcessingException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
		.when()
	       .put("api/survey-unit/12/state/VIN")
		.then()
			.statusCode(200);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 400 with unknown state
 	 * @throws InterruptedException
	 */
	@Test
	@Order(21)
	public void testPutSurveyUnitStateStateFalse() throws InterruptedException, JSONException, JsonProcessingException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
		.when()
			.put("api/survey-unit/11/state/test")
		.then()
			.statusCode(400);
	}
	
	/**
	 * Test that the PUT endpoint "api/survey-unit/{id}"
	 * return 403 when not allowed to pass to this state
 	 * @throws InterruptedException
	 */
	@Test
	@Order(22)
	public void testPutSurveyUnitStateNoSu() throws InterruptedException, JSONException, JsonProcessingException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		List<String> listSu = new ArrayList<>();
		listSu.add("");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
		.when()
			.put("api/survey-unit/11/state/AOC")
		.then()
			.statusCode(403);
	}
	
	/**
	 * Test that the PUT endpoint "api/preferences"
	 * return 200
 	 * @throws InterruptedException
	 */
	@Test
	@Order(23)
	public void testPutPreferences() throws InterruptedException, JSONException, JsonProcessingException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		List<String> listPreferences = new ArrayList<>();
		listPreferences.add("simpsons2020x00");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(listPreferences))
		.when()
			.put("api/preferences")
		.then()
			.statusCode(200);
	}
	
	/**
	 * Test that the PUT endpoint "api/preferences"
	 * return 200
 	 * @throws InterruptedException
	 */
	@Test
	@Order(24)
	public void testPutPreferencesWrongCampaignId() throws InterruptedException, JSONException, JsonProcessingException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		List<String> listPreferences = new ArrayList<>();
		listPreferences.add("");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body(new ObjectMapper().writeValueAsString(listPreferences))
		.when()
			.put("api/preferences")
		.then()
			.statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/abandoned"
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(25)
	public void testGetNbSuAbandoned() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken)
		.when().get("api/campaign/simpsons2020x00/survey-units/abandoned")
		.then()
		.statusCode(200).and()
		.assertThat().body("count", equalTo(0));
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/abandoned"
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(26)
	public void testGetNbSuAbandonedNotFound() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken)
		.when().get("api/campaign/test/survey-units/abandoned")
		.then()
		.statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/not-attributed"
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(27)
	public void testGetNbSuNotAttributed() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken)
		.when().get("api/campaign/simpsons2020x00/survey-units/not-attributed")
		.then()
		.statusCode(200).and()
		.assertThat().body("count", equalTo(0));
	}
	
	/**
	 * Test that the GET endpoint "api/campaign/{id}/survey-units/not-attributed"
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	@Order(28)
	public void testGetNbSuNotAttributedNotFound() throws InterruptedException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken)
		.when().get("api/campaign/test/survey-units/not-attributed")
		.then()
		.statusCode(404);
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{id}/collection-dates"
	 * return 200 when modifying both dates
	 * @throws InterruptedException
	 */
	@Test
	@Order(29)
	public void testPutCollectionDatesModifyBothDates() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{\"startDate\": 162849200000, \"endDate\": 170849200000}")
		.when()
			.put("api/campaign/simpsons2020x00/collection-dates")
		.then()
      .statusCode(200);
    Optional<Campaign> simpsons = campaignRepository.findByIdIgnoreCase("simpsons2020x00");
    assertEquals(true, simpsons.isPresent());
    assertEquals(162849200000L, simpsons.get().getStartDate());
    assertEquals(170849200000L, simpsons.get().getEndDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{id}/collection-dates"
	 * return 200 when modifying start date
	 * @throws InterruptedException
	 */
	@Test
	@Order(30)
	public void testPutCollectionDatesModifyStartDate() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{\"startDate\": 162849200000}")
		.when()
			.put("api/campaign/simpsons2020x00/collection-dates")
		.then()
      .statusCode(200);
    Optional<Campaign> simpsons = campaignRepository.findByIdIgnoreCase("simpsons2020x00");
    assertEquals(true, simpsons.isPresent());
    assertEquals(162849200000L, simpsons.get().getStartDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{id}/collection-dates"
	 * return 200 when modifying end date
	 * @throws InterruptedException
	 */
	@Test
	@Order(31)
	public void testPutCollectionDatesModifyEndDate() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{\"endDate\": 170849200000}")
		.when()
			.put("api/campaign/simpsons2020x00/collection-dates")
		.then()
      .statusCode(200);
    Optional<Campaign> simpsons = campaignRepository.findByIdIgnoreCase("simpsons2020x00");
    assertEquals(true, simpsons.isPresent());
    assertEquals(170849200000L, simpsons.get().getEndDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{id}/collection-dates"
	 * return 400 when empty body
	 * @throws InterruptedException
	 */
	@Test
	@Order(32)
	public void testPutCollectionDatesEmptyBody() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{}")
		.when()
			.put("api/campaign/simpsons2020x00/collection-dates")
		.then()
      .statusCode(400);
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{id}/collection-dates"
	 * return 400 when bad format
	 * @throws InterruptedException
	 */
	@Test
	@Order(33)
	public void testPutCollectionDatesBadFormat() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{\"startDate\": 162849200000, \"endDate\": \"23/05/2020\"}")
		.when()
			.put("api/campaign/simpsons2020x00/collection-dates")
		.then()
      .statusCode(400);
  }

	/**
	 * Test that the PUT endpoint "api/campaign/{idCampaign}/organizational-unit/{idOu}/visibility"
	 * return 200 when modifying all dates
	 * @throws InterruptedException
	 */
	@Test
	@Order(34)
	public void testPutVisibilityModifyAllDates() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
		 	.body("{\"managementStartDate\": 1575937000000 , "
					+ "\"interviewerStartDate\": 1576801000000,"
					+ "\"identificationPhaseStartDate\": 1577233000000,"
					+ "\"collectionStartDate\": 1577837800000,"
					+ "\"collectionEndDate\": 1640996200000,"
					+ "\"endDate\": 1641514600000}")
		.when()
			.put("api/campaign/simpsons2020x00/organizational-unit/OU-NORTH/visibility")
		.then()
      .statusCode(200);
    Optional<Visibility> visi = visibilityRepository.findVisibilityByCampaignIdAndOuId("simpsons2020x00", "OU-NORTH");
    assertEquals(true, visi.isPresent());
    assertEquals(1575937000000L, visi.get().getManagementStartDate());
    assertEquals(1576801000000L, visi.get().getInterviewerStartDate());
    assertEquals(1577233000000L, visi.get().getIdentificationPhaseStartDate());
    assertEquals(1577837800000L, visi.get().getCollectionStartDate());
    assertEquals(1640996200000L, visi.get().getCollectionEndDate());
    assertEquals(1641514600000L, visi.get().getEndDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{idCampaign}/organizational-unit/{idOu}/visibility"
	 * return 200 when modifying start date
	 * @throws InterruptedException
	 */
	@Test
	@Order(35)
	public void testPutVisibilityModifyCollectionStartDate() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{\"collectionStartDate\": 1577847800000}")
		.when()
    .put("api/campaign/simpsons2020x00/organizational-unit/OU-NORTH/visibility")
		.then()
      .statusCode(200);
    Optional<Visibility> visi = visibilityRepository.findVisibilityByCampaignIdAndOuId("simpsons2020x00", "OU-NORTH");
    assertEquals(true, visi.isPresent());
    assertEquals(1577847800000L, visi.get().getCollectionStartDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{idCampaign}/organizational-unit/{idOu}/visibility"
	 * return 200 when modifying end date
	 * @throws InterruptedException
	 */
	@Test
	@Order(36)
	public void testPutVisibilityModifyCollectionEndDate() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 .contentType("application/json")
			.body("{\"collectionEndDate\": 1577857800000}")
		.when()
			.put("api/campaign/simpsons2020x00/organizational-unit/OU-NORTH/visibility")
		.then()
      .statusCode(200);
    Optional<Visibility> visi = visibilityRepository.findVisibilityByCampaignIdAndOuId("simpsons2020x00", "OU-NORTH");
    assertEquals(true, visi.isPresent());
    assertEquals(1577857800000L, visi.get().getCollectionEndDate());
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{idCampaign}/organizational-unit/{idOu}/visibility"
	 * return 400 when empty body
	 * @throws InterruptedException
	 */
	@Test
	@Order(37)
	public void testPutVisibilityEmptyBody() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
		 	.contentType("application/json")
			.body("{}")
		.when()
    .put("api/campaign/simpsons2020x00/organizational-unit/OU-NORTH/visibility")
		.then()
      .statusCode(400);
  }
  
  /**
	 * Test that the PUT endpoint "api/campaign/{idCampaign}/organizational-unit/{idOu}/visibility"
	 * return 400 when bad format
	 * @throws InterruptedException
	 */
	@Test
	@Order(38)
	public void testPutVisibilityBadFormat() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		 given().auth().oauth2(accessToken)
	 	.contentType("application/json")
		.body("{\"managementStartDate\": 1640996200000, "
				+ "\"interviewerStartDate\": \"10/10/2020\","
				+ "\"identificationPhaseStartDate\": 1641514600000,"
				+ "\"collectionStartDate\": 1577233000000,"
				+ "\"collectionEndDate\": 1576801000000,"
				+ "\"endDate\": 1575937000000}")
		.when()
      .put("api/campaign/simpsons2020x00/organizational-unit/OU-NORTH/visibility")
		.then()
      .statusCode(400);
	}
	
	/**
	 * Test that the POST endpoint "api/message" return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(39)
	public void testPostMessage() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		List<String> recipients = new ArrayList<String>();
		recipients.add("INTW1");
		MessageDto message = new MessageDto("TEST", recipients);
		message.setSender("abc");
		given().auth().oauth2(accessToken).contentType("application/json")
				.body(new ObjectMapper().writeValueAsString(message)).when().post("api/message").then().statusCode(200);
		List<MessageDto> messages = messageRepository
				.findMessagesDtoByIds(messageRepository.getMessageIdsByInterviewer("INTW1"));
		assertEquals("TEST", messages.get(0).getText());
	}

	/**
	 * Test that the POST endpoint "api/message" return 400 when bad format
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(40)
	public void testPostMessageBadFormat() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).contentType("application/json")
				.body(new ObjectMapper().writeValueAsString(null)).when().post("api/message").then().statusCode(400);
	}
	
	/**
	 * Test that the GET endpoint
	 * "api/messages/{id}" return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(41)
	public void testGetMessage() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		given().auth().oauth2(accessToken).when().get("api/messages/INTW1").then().statusCode(200).and()
		.assertThat().body("text", hasItem("TEST"));		
	}
	
	/**
	 * Test that the GET endpoint
	 * "api/messages/{id}" return empty body with a wrong id
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(42)
	public void testGetMessageWrongId() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "INTW1", "a");
		given().auth().oauth2(accessToken).when().get("api/messages/123456789").then().statusCode(200).and()
		.assertThat().body("isEmpty()", Matchers.is(true));		
	}

	/**
	 * Test that the put endpoint "api/message/{id}/interviewer/{idep}/read" 
	 * return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(43)
	public void testPutMessageAsRead() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "intw1", "a");
		Long messageId = messageRepository.getMessageIdsByInterviewer("INTW1").get(0);
		given().auth().oauth2(accessToken).contentType("application/json").when()
				.put("api/message/" + messageId + "/interviewer/INTW1/read").then().statusCode(200);
		Optional<Message> message = messageRepository.findById(messageId);
		assertEquals(MessageStatusType.REA, message.get().getMessageStatus().get(0).getStatus());
	}
	
	/**
	 * Test that the put endpoint "api/message/{id}/interviewer/{idep}/delete" 
	 * return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(44)
	public void testPutMessageAsDelete() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "intw1", "a");
		Long messageId = messageRepository.getMessageIdsByInterviewer("INTW1").get(0);
		given().auth().oauth2(accessToken).contentType("application/json").when()
				.put("api/message/" + messageId + "/interviewer/INTW1/delete").then().statusCode(200);
		Optional<Message> message = messageRepository.findById(messageId);
		assertEquals(MessageStatusType.DEL, message.get().getMessageStatus().get(0).getStatus());
	}

	/**
	 * Test that the PUT endpoint
	 * "/message/{id}/interviewer/{idep}/read" return 404 with a wrong Id
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(45)
	public void testPutMessageAsReadWrongId() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "intw1", "a");
		Long messageId = messageRepository.getMessageIdsByInterviewer("INTW1").get(0);
		given().auth().oauth2(accessToken).contentType("application/json").when()
				.put("api/message/" + messageId + "/interviewer/Test/read").then().statusCode(404);
	}

	/**
	 * Test that the GET endpoint
	 * "/message-history" return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(46)
	public void testGetMessageHistory() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		given().auth().oauth2(accessToken).when().get("api/message-history").then().statusCode(200).and()
				.assertThat().body("text", hasItem("TEST"));
	}

	/**
	 * Test that the POST endpoint
	 * "/verify-name" return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(47)
	public void testPostVerifyName() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "abc", "a");
		WsText message = new WsText("INTW1");
		given().auth().oauth2(accessToken).contentType("application/json")
				.body(new ObjectMapper().writeValueAsString(message)).when().post("api/verify-name").then()
				.statusCode(200).and().assertThat().body("id", hasItem("INTW1"));
	}
	
	/**
	 * Test that the POST endpoint "api/message" return 200
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@Order(48)
	public void testPostMessageSysteme() throws InterruptedException, JsonProcessingException, JSONException {
		String accessToken = resourceOwnerLogin(CLIENT, CLIENT_SECRET, "intw1", "a");
		List<String> recipients = new ArrayList<String>();
		recipients.add("INTW2");
		MessageDto message = new MessageDto("Synchronisation", recipients);
		given().auth().oauth2(accessToken).contentType("application/json")
				.body(new ObjectMapper().writeValueAsString(message)).when().post("api/message").then().statusCode(200);
		List<MessageDto> messages = messageRepository
				.findMessagesDtoByIds(messageRepository.getMessageIdsByInterviewer("INTW2"));
		assertEquals("Synchronisation", messages.get(0).getText());
	}
	
}
