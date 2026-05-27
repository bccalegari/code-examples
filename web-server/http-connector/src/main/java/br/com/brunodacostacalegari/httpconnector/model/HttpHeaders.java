package br.com.brunodacostacalegari.httpconnector.model;

import java.util.*;

public class HttpHeaders {
    private final Map<String, List<String>> values = new LinkedHashMap<>();
    private final Map<String, String> originalNames = new HashMap<>();

    public void set(String name, String value) {
        String key = normalize(name);
        originalNames.putIfAbsent(key, name);
        values.put(key, new ArrayList<>(List.of(value)));
    }

    public void add(String name, String value) {
        String key = normalize(name);
        originalNames.putIfAbsent(key, name);
        values.computeIfAbsent(key, _ -> new ArrayList<>()).add(value);
    }

    public List<String> get(String name) {
        return List.copyOf(values.getOrDefault(normalize(name), List.of()));
    }

    public boolean contains(String name) {
        return values.containsKey(normalize(name));
    }

    public Set<String> names() {
        return new LinkedHashSet<>(originalNames.values());
    }

    public Set<Map.Entry<String, List<String>>> entries() {
        return Set.copyOf(values.entrySet());
    }

    public String getOriginalName(String normalized) {
        return originalNames.get(normalized);
    }

    public void clear() {
        values.clear();
        originalNames.clear();
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
