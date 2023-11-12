package nl.softworks.calendarAggregator.domain.entity;

import nl.softworks.calendarAggregator.domain.boundary.R;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CalendarSourceRegexScraperTest {

    @Test
    public void test() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<CalendarEvent> calendarEvents = new CalendarSourceRegexScraper()
                .content("""
                        DANSAVOND
                         za. 18 november 2023
                         van 20:00 tot 23:59 uur
                        Voor alle ballroom- & Latin dansers is er op zaterdag weer een gezellige dansavond bij Citydance!
                        Zien we je daar?
                              
                        Tijd: 20.00 - 00.00
                        Entree: € 7,50,- per persoon
                              
                        Heb je danservaring maar ben je geen lid?
                        Of heb je helemaal geen danservaring maar wil je wel gewoon gezellig langskomen?
                        Je bent natuurlijk van harte welkom!
                        Stuur ons gerust een mailtje.
                              
                        Pietendansfeest
                         za. 25 november 2023
                         van 10:30 tot 12:00 uur
                        Op zaterdag 25 november nodigen wij daarom de kleinste kinderen graag uit voor ons Pieten Dansfeest! (3 tot en met 8 jaar)
                        We starten om 10.30 uur op locatie Citydance (Varsseveldseweg 89, Doetinchem)
                        Wanneer alle dansjes goed gedanst zijn komen misschien zelfs de pieten wel om de kinderen te verblijden met een kadootje en natuurlijk niet te vergeten, pepernoten!
                              
                        Rond 12.00 uur zullen de pieten weer op doorreis gaan en zwaaien we iedereen uit!
                        Kosten € 10,00 per kind (contant te voldoen) op 25 november aan de deur.
                        Uiteraard zijn vriendjes/vriendinnetjes ook van harte welkom!
                              
                        Aanmelden VOOR 23 november (voor leden via de Citydance app en niet leden via mail aanmelden) zodat de Piet weet hoeveel kadootjes hij mee moet nemen.
                        Vermeld bij aanmelding de naam en leeftijd en ook graag van eventuele vriendjes/vriendinnetjes
                        Mailen naar:  info@citydance.nl
                              
                        Kerstgala
                         za. 16 december 2023
                         van 20:30 tot 23:59 uur
                        16 December het Kerstgala !!
                              
                        We kijken er alweer heel erg naar uit.
                              
                        Meld je aan via je leden app of stuur een mailtje naar info@citydance.nl
                        Natuurlijk zijn introducees en niet-leden ook van harte welkom.
                              
                        Laat je verrassen door mooie optredens tijdens de avond, wij zorgen voor de hapjes en uiteraard zal een lekker welkomstdrankje niet ontbreken.
                              
                        Dresscode; Gala
                              
                        Betaling contant te voldoen bij aanvang € 22,50
                              
                        Deuren zijn open vanaf 20.00 uur\s
                        """)
                .regex("([a-zA-Z]*) +[a-z]{2}\\. ([0-9][0-9]? +(november|december) +[0-9]{4}) +van ([0-9]+:[0-9]+) tot ([0-9]+:[0-9]+)")
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);
    }
}
