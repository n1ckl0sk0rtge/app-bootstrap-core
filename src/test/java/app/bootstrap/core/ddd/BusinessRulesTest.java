package app.bootstrap.core.ddd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BusinessRules(rules = {
        "This should not be empty",
        "This should not be empty too"})
class BusinessRulesTest {

    @Test
    void shouldHaveBusinessRulesAnnotation() {
        BusinessRules annotation = BusinessRulesTest.class.getAnnotation(BusinessRules.class);
        assertNotNull(annotation, "BusinessRules annotation should be present");
        assertArrayEquals(
                new String[]{"This should not be empty", "This should not be empty too"},
                annotation.rules(),
                "BusinessRules should have correct rules defined"
        );
    }

}
