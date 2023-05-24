package com.example.TeamPlaningToolBackend.utils;

public class JaroWinkler {

    public static float compare(String s1, String s2) {
        int s1Length = s1.length(), s2Length = s2.length();

        if(s1Length == 0 && s2Length == 0) return 1;

        int matchDistance = (Math.max(s1Length, s2Length) / 2) - 1;
        boolean[] s1Matches = new boolean[s1Length];
        boolean[] s2Matches = new boolean[s2Length];

        float matches = 0;
        int transpositions = 0;

        for(int i = 0; i < s1Length; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, s2Length);

            for(int j = start; j < end; j++) {
                if(s2Matches[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s2Matches[j] = true;
                s1Matches[i] = true;
                matches++;
                break;
            }
        }

        if(matches == 0) return 0;

        int k = 0;
        for(int i = 0; i < s1Length; i++) {
            if(!s1Matches[i]) continue;
            while(!s2Matches[k]) k++;
            if(s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        int prefixLength = 0;
        while(prefixLength < 4 && s1.charAt(prefixLength) == s2.charAt(prefixLength)) prefixLength++;

        float score = ((matches / s1Length) + (matches / s2Length) + ((matches - transpositions / 2.f) / matches)) / 3.f;
        return score + 0.1f * Math.min(prefixLength, 4) * (1 - score);
    }
}