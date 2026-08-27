package com.joy.spotify_clone.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.joy.spotify_clone.service.GenericGeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GenericGeminiServiceImpl implements GenericGeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GenericGeminiServiceImpl.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.models}")
    private String geminiModels;

    private final ObjectMapper objectMapper;

    public GenericGeminiServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        if(prompt == null || prompt.trim().isEmpty()){
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        Client client = new Client.Builder().apiKey(geminiApiKey).build();
        String[] models = geminiModels.split(",");
        Exception lastException = null;
        for(int i = 0; i < models.length; i++){
            try{
                logger.info("Calling Gemini API with model: {} ({}/{})", models[i].trim(), i+1, models.length);
                String response = client.models.generateContent(models[i].trim(), prompt, null).text();
                if(response == null || response.isEmpty()){
                    throw new RuntimeException("Response cannot be null or empty");
                }
                return parseResponse(response, responseType);
            }catch(ClientException ex){
                if(ex.getMessage() != null &&
                    (
                        ex.getMessage().contains("429") ||
                        ex.getMessage().contains("404") ||
                        ex.getMessage().toLowerCase().contains("no longer available")
                    )){
                    if(ex.getMessage().contains("429"))logger.warn("Rate limit exceeded for model: {}. Trying next model if available.", models[i].trim());
                    else if(ex.getMessage().contains("404") || ex.getMessage().toLowerCase().contains("no longer available")) logger.warn("Model {} is no longer available. Trying next model if available.", models[i].trim());
                    lastException = ex;
                    if(i < models.length - 1) continue;
                }
                else{
                    throw new RuntimeException("Gemini API error: "+ex.getMessage(), ex);
                }
            }

        }
        throw new RuntimeException("All models exhausted. Last error: "+(lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private <T> T parseResponse(String response, Class<T> responseType) {
        if(responseType == String.class){
            return responseType.cast(response);
        }
        try{
            String json = response.trim();
            if(json.startsWith("```json")) json = json.substring(7);
            else if(json.startsWith("```")) json = json.substring(3);
            if(json.endsWith("```")) json = json.substring(0, json.length() - 3);
            return objectMapper.readValue(json.trim(), responseType);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage(), e);
        }
    }
}
/*
Working of the generateContent method:
1. Input Validation: The method first checks if the provided prompt is null or empty.
If it is, an IllegalArgumentException is thrown to ensure that the method always receives a valid prompt.
2. Client Initialization: A new instance of the Gemini API client is created using the provided API key. This client will be used to make requests to the Gemini API.
3. Model Iteration: The method retrieves the list of models from the configuration and splits them into an array. It then iterates over each model to attempt generating content.
4. API Call: For each model, the method calls the Gemini API's generateContent method with the current model and the provided prompt. It logs the model being used and its position in the list for debugging purposes.
5. Response Handling: If the response from the API is null or empty, a RuntimeException is thrown. If a valid response is received, it is passed to the parseResponse method to convert it into the desired response type.
6. Error Handling: If a ClientException is caught, the method checks if the error message indicates a rate limit issue (HTTP 429) or a model unavailability issue. If so, it logs a warning and continues to the next model in the list. If the error is not related to rate limiting, a RuntimeException is thrown with the error message.
7. Exhaustion of Models: If all models have been tried and none succeeded (due to rate limiting or other errors), a RuntimeException is thrown indicating that all models have been exhausted, along with the last error message encountered.
 */
/*
Working of the parseResponse method:
1. Type Checking: The method first checks if the desired response type is String. If it is, the response is cast to String and returned directly.
2. JSON Parsing: If the response type is not String, the method attempts to parse the response as JSON. It trims any leading or trailing whitespace and checks for code block markers (```json or ```). If such markers are found, they are removed to isolate the JSON content.
3. Object Mapping: The cleaned JSON string is then passed to the ObjectMapper's readValue method, which converts the JSON into an instance of the specified response type.
4. Error Handling: If any exception occurs during the parsing process, a RuntimeException is thrown with a message indicating the error, along with the original exception for debugging purposes.
 */
