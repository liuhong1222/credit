package cn.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * Description:Jackson工具类
 * User: liutao
 * Date: 2018-02-21
 * Time: 16:08
 */
@Component
public final class JacksonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JacksonUtil.class);

    //    public static ObjectMapper objectMapper;
    private static Gson gson;
    private static JsonParser jsonParser = new JsonParser();

    @Autowired
    public void setGson(final Gson gson) {
        JacksonUtil.gson = gson;
    }


    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        return exec(() -> gson.fromJson(jsonStr, valueType));
    }
    
    
    public static <T> T readValue(String jsonStr, Type type) {
        return exec(() -> gson.fromJson(jsonStr, type));
    }


    /**
     * 把Object转换为json字符串
     */
    public static String toJson(Object object) {
        return exec(() -> gson.toJson(object));
    }


    /**
     * 把Object转换为json字符串
     */
    public static String toJson(Integer[] ints) {
        return exec(() -> gson.toJson(ints));
    }


    /**
     * json string to JsonNode
     *
     * @param jsonStr
     * @return JsonNode
     */
    public static JsonObject toJsonNode(String jsonStr) {
        return exec(() -> jsonParser.parse(jsonStr).getAsJsonObject());
    }


    private static <T> T exec(Function<T> function) {
        try {
            return function.apply();
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }


    @FunctionalInterface
    private static interface Function<T> {
        T apply();
    }


}