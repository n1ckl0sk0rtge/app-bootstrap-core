package app.bootstrap.core.ddd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BusinessRule(rule = "This should not be empty")
class BusinessRuleTest {

    @Test
    void shouldHaveBusinessRuleAnnotation() {
        BusinessRule annotation = BusinessRuleTest.class.getAnnotation(BusinessRule.class);
        assertNotNull(annotation, "BusinessRuleTest should be annotated with @BusinessRule");
        assertEquals("This should not be empty", annotation.rule(), "BusinessRule rule value should match");
    }

}
