package hu.peterszrnka.springboottests;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.util.AssertionErrors.*;

public class IntegrationAnnotationCheckerTest {

    private static final Set<String> IGNORED_METHODS = Set.of(
            "$jacocoInit", "equals", "hashCode", "toString", "notify", "notifyAll", "wait", "getClass", "finalize", "wait0", "clone"
    );
    private static final Map<String, TestClassData> integrationTests;

    static {
        try {
            integrationTests = getAllIntegrationTestClasses();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldControllerHaveProperIntegrationTests() throws Exception {
        Map<String, ClassData> controllers = getAllControllerClasses();
        controllers.forEach(IntegrationAnnotationCheckerTest::assertController);
    }

    @Getter
    @Setter
    static class ClassData {
        private Set<String> methods = new HashSet<>();
    }

    @Getter
    @Setter
    static class TestClassData {
        private String testClassName;
        private Set<String> methods = new HashSet<>();
        private boolean skip;
    }

    private static Map<String, ClassData> getAllControllerClasses() throws Exception {
        Map<String, ClassData> resultMap = new HashMap<>();
        Set<Class<?>> controllers = getAllSubClasses(BaseController.class);

        for (Class<?> controller : controllers) {
            ClassData classData = new ClassData();
            Set<String> controllerMethods = Stream.of(controller.getDeclaredMethods())
                    .map(Method::getName)
                    .filter(name -> !name.startsWith("lambda$")).collect(Collectors.toSet());

            controllerMethods.addAll(Stream.of(controller.getSuperclass().getDeclaredMethods())
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .filter(method -> !IGNORED_METHODS.contains(method.getName()) && !method.getName().contains("$"))
                    .map(Method::getName)
                    .collect(Collectors.toSet()));

            classData.setMethods(controllerMethods);
            resultMap.put(controller.getSimpleName(), classData);
        }

        return resultMap;
    }

    private static Map<String, TestClassData> getAllIntegrationTestClasses() throws Exception {
        return getAllSpecificTestClasses();
    }


    private static Map<String, TestClassData> getAllSpecificTestClasses() throws Exception {
        Map<String, TestClassData> resultMap = new HashMap<>();
        Set<Class<?>> testClasses = getAllSubClasses(BaseIntegrationTest.class);

        for (Class<?> test : testClasses) {
            TestedClass testedClassAnnotation = test.getAnnotation(TestedClass.class);
            assertNotNull("Annotation @TestedClass is missing from " + test.getSimpleName(), testedClassAnnotation);
            Class<?> originalClass = testedClassAnnotation.value();
            boolean skip = testedClassAnnotation.skip();

            TestClassData classData = new TestClassData();
            Set<String> testMethods = Stream.of(test.getDeclaredMethods())
                    .filter(method -> method.getAnnotation(TestedMethod.class) != null)
                    .map(methods -> {
                        TestedMethod testedMethodAnnotation = methods.getAnnotation(TestedMethod.class);
                        return testedMethodAnnotation.value();
                    })
                    .collect(Collectors.toSet());
            classData.setTestClassName(test.getSimpleName());
            classData.setMethods(testMethods);
            classData.setSkip(skip);
            resultMap.put(originalClass.getSimpleName(), classData);
        }

        return resultMap;
    }

    private static Set<Class<?>> getAllSubClasses(Class<?> inputClazz) {
        Reflections reflections = new Reflections("hu.peterszrnka.springboottests");
        return reflections.getSubTypesOf(inputClazz).stream().filter(cls -> !Modifier.isAbstract(cls.getModifiers())).collect(Collectors.toSet());
    }

    private static void assertController(
            String key,
            ClassData classData) {
        assertTrue("Integration test is missing for " + key, integrationTests.containsKey(key));

        assertEquals(key + " has some untested methods!", postFilterMethods(classData.getMethods()), integrationTests.get(key).getMethods());
    }

    private static Set<String> postFilterMethods(Set<String> methodNames) {
        return methodNames.stream().filter(name -> !name.contains("$")).collect(Collectors.toSet());
    }

    private static String printUncoveredTestMethods(Map<String, Set<String>> missingSecurityTestMethods) {
        StringBuilder sb = new StringBuilder("\r\n");
        missingSecurityTestMethods.forEach((k, v) -> {
            sb.append(k).append(": ").append(v).append("\r\n");
        });

        return sb.toString();
    }
}
