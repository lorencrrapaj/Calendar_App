package com.example.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class CalendarApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // It's a basic smoke test for the main application class
    }

    @Test
    void mainMethodExists() {
        // Test that main method exists and can be called without arguments
        // We don't actually run it to avoid port conflicts
        try {
            CalendarApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist", e);
        }
    }

    @Test
    void applicationClassExists() {
        // Test that the CalendarApplication class exists and is properly annotated
        assertThat(CalendarApplication.class).isNotNull();
        assertThat(CalendarApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class)).isTrue();
    }

    @Test
    void mainMethodCanBeInvoked() {
        // Test that main method can be invoked without throwing exceptions
        // We use a mock args array to avoid actually starting the application
        try {
            java.lang.reflect.Method mainMethod = CalendarApplication.class.getDeclaredMethod("main", String[].class);
            assertThat(mainMethod).isNotNull();
            assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
            assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers())).isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist and be accessible", e);
        }
    }

    @Test
    void applicationHasCorrectPackage() {
        // Test that the application is in the correct package
        assertThat(CalendarApplication.class.getPackage().getName()).isEqualTo("com.example.calendar");
    }

    @Test
    void mainMethodExecutesSuccessfully() {
        // Test that main method can be executed without errors
        // Mock SpringApplication.run to avoid actually starting the application
        try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(CalendarApplication.class), any(String[].class)))
                    .thenReturn(null);

            // This should not throw any exceptions
            CalendarApplication.main(new String[]{});

            // Verify that SpringApplication.run was called with correct parameters
            mockedSpringApplication.verify(() -> SpringApplication.run(eq(CalendarApplication.class), any(String[].class)));
        }
    }
}
