package com.joy.spotify_clone.service;

public interface GenericGeminiService {
    <T> T generateContent(String prompt, Class<T> responseType);
}
