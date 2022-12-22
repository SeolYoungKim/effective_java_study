
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class SimpleAssert extends Assert {

  public static void state(boolean expression, String message,
      final Class<? extends RuntimeException> exceptionClass) {
    if (!expression) {
      throwException(message, exceptionClass);
    }
  }

  public static void isTrue(boolean expression, String message,
      final Class<? extends RuntimeException> exceptionClass) {
    if (!expression) {
      throwException(message, exceptionClass);
    }
  }

  public static void notNull(Object object, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (object == null) {
      throwException(message, exceptionClass);
    }
  }

  public static void isNull(@Nullable Object object, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (object != null) {
      throwException(message, exceptionClass);
    }
  }

  public static void hasLength(@Nullable String text, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (!StringUtils.hasLength(text)) {
      throwException(message, exceptionClass);
    }
  }

  public static void notEmpty(@Nullable Object[] array, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (ObjectUtils.isEmpty(array)) {
      throwException(message, exceptionClass);
    }
  }

  public static void notEmpty(@Nullable Collection<?> collection, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (CollectionUtils.isEmpty(collection)) {
      throwException(message, exceptionClass);
    }
  }

  public static void notEmpty(@Nullable Map<?, ?> map, String message, final Class<? extends RuntimeException> exceptionClass) {
    if (CollectionUtils.isEmpty(map)) {
      throwException(message, exceptionClass);
    }
  }


  private static void throwException(String message,
      final Class<? extends RuntimeException> exceptionClass) {
    try {
      throw exceptionClass.getDeclaredConstructor(String.class).newInstance(message);
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException("Exception raised while Exception handling. " + e.getMessage());
    }
  }
}
