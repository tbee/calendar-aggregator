package nl.softworks.calendarAggregator.giwth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:testHsqldbMEM.properties")
class GiwthTest { //extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void test() {
//        Scenario.of("Modify Vacation Hours", context() )
//
//                .given( ShiftType.standardSetExists() )
//                .and( RosterPeriod.startingOn("2022-09-19").exists() )
//                .and( User.user().isLoggedin() )
//
//                .when( Overview.ofRosterPeriod("2022-09-19").isAccessed() )
//                .and( VacationHours.forUser("user").onDate("2022-09-19").isSetTo(20) )
//
//                .then( VacationHours.forUser("user").onDate("2022-09-19").shouldBe(20) )
//                .and( WeekTotals.forUser("user").inRosterPeriod("2022-09-19").shouldBe(20,0,0,0,0,0) )
//                .and( RunningWeekTotals.forUser("user").inRosterPeriod("2022-09-19").shouldBe(20,20,20,20,20,20) )
//                .and( Event.who("user").what("SetVacationHours").user("user").rosterDate("2022-09-19").detailSubstring("hours=20").shouldExist() );
    }

//    private StepContext context() {
//        StepContext stepContext = new StepContext(port, page);
//        beanFactory.autowireBean(stepContext);
//        return stepContext;
//    }
}
